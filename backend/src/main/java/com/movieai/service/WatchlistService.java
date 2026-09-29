package com.movieai.service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;

import com.movieai.config.MovieAiProperties;
import com.movieai.dto.MediaSummary;
import com.movieai.dto.MediaType;
import com.movieai.dto.MovieDetails;
import com.movieai.dto.MovieSummary;
import com.movieai.dto.PagedResponse;
import com.movieai.dto.TvDetails;
import com.movieai.dto.TvSummary;
import com.movieai.dto.watchlist.WatchlistDtos.WatchlistItem;
import com.movieai.dto.watchlist.WatchlistDtos.WatchlistKey;
import com.movieai.exception.ApiException;
import com.movieai.repository.WatchlistItemRepository;
import com.movieai.service.catalog.MovieCatalog;
import com.movieai.service.catalog.TvCatalog;
import com.movieai.service.image.ImageUrlResolver;
import com.movieai.service.image.ImageUrlResolver.Kind;
import com.movieai.service.image.ImageUrlResolver.Size;
import com.movieai.dto.Genre;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persistent per-user watchlist. Only (media type, TMDB id) is stored; display data is resolved
 * from the shared public details cache, so nothing user-specific is ever cached.
 */
@Service
public class WatchlistService {

    private static final int MAX_CONCURRENT_LOOKUPS = 8;

    private final WatchlistItemRepository repository;
    private final MovieCatalog movies;
    private final TvCatalog tv;
    private final ImageUrlResolver images;
    private final ExecutorService executor;
    private final String defaultRegion;

    public WatchlistService(WatchlistItemRepository repository, MovieCatalog movies, TvCatalog tv,
                            ImageUrlResolver images, ExecutorService fanOutExecutor, MovieAiProperties properties) {
        this.repository = repository;
        this.movies = movies;
        this.tv = tv;
        this.images = images;
        this.executor = fanOutExecutor;
        this.defaultRegion = properties.tmdb().defaultRegion();
    }

    public record AddResult(WatchlistItem item, boolean created) {
    }

    /** Not transactional: no DB connection is held while display data is fetched. */
    public PagedResponse<WatchlistItem> list(UUID userId, MediaType type, int page, int size) {
        PageRequest pageable = PageRequest.of(page - 1, size);
        Page<com.movieai.entity.WatchlistItem> items = type == null
                ? repository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                : repository.findByUserIdAndMediaTypeOrderByCreatedAtDesc(userId, type, pageable);

        Semaphore permits = new Semaphore(MAX_CONCURRENT_LOOKUPS);
        List<CompletableFuture<WatchlistItem>> futures = items.getContent().stream()
                .map(item -> CompletableFuture.supplyAsync(() -> {
                    permits.acquireUninterruptibly();
                    try {
                        return toDto(item, resolve(item.getMediaType(), item.getTmdbId()));
                    } finally {
                        permits.release();
                    }
                }, executor))
                .toList();
        List<WatchlistItem> results = futures.stream().map(CompletableFuture::join).toList();
        return new PagedResponse<>(results, page, items.getTotalPages(), (int) items.getTotalElements());
    }

    @Transactional(readOnly = true)
    public List<WatchlistKey> keys(UUID userId) {
        return repository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(i -> new WatchlistKey(i.getMediaType(), i.getTmdbId()))
                .toList();
    }

    /**
     * Idempotent. Not transactional so a unique-constraint race can be recovered from, and no DB
     * connection is held while the id is validated upstream.
     */
    public AddResult add(UUID userId, MediaType type, long tmdbId) {
        var existing = repository.findByUserIdAndMediaTypeAndTmdbId(userId, type, tmdbId);
        if (existing.isPresent()) {
            return new AddResult(toDto(existing.get(), resolve(type, tmdbId)), false);
        }
        // Throws a not-found error for ids that don't exist upstream.
        MediaSummary media = type == MediaType.MOVIE ? summary(movies.details(tmdbId, defaultRegion))
                : summary(tv.details(tmdbId, defaultRegion));
        try {
            var saved = repository.saveAndFlush(new com.movieai.entity.WatchlistItem(userId, type, tmdbId));
            return new AddResult(toDto(saved, media), true);
        } catch (DataIntegrityViolationException e) {
            var concurrent = repository.findByUserIdAndMediaTypeAndTmdbId(userId, type, tmdbId).orElseThrow(() -> e);
            return new AddResult(toDto(concurrent, media), false);
        }
    }

    @Transactional
    public void remove(UUID userId, MediaType type, long tmdbId) {
        repository.deleteItem(userId, type, tmdbId);
    }

    @Transactional(readOnly = true)
    public boolean contains(UUID userId, MediaType type, long tmdbId) {
        return repository.existsByUserIdAndMediaTypeAndTmdbId(userId, type, tmdbId);
    }

    /** Display data is best-effort: if the provider is unavailable the item is still listed. */
    private MediaSummary resolve(MediaType type, long tmdbId) {
        try {
            return type == MediaType.MOVIE ? summary(movies.details(tmdbId, defaultRegion))
                    : summary(tv.details(tmdbId, defaultRegion));
        } catch (ApiException e) {
            return null;
        }
    }

    private MovieSummary summary(MovieDetails d) {
        return new MovieSummary(d.id(), MediaType.MOVIE, d.title(), d.originalTitle(), d.overview(), d.releaseDate(),
                d.year(), d.rating(), d.voteCount(), 0, d.genres().stream().map(Genre::id).toList(),
                d.genres().stream().map(Genre::name).toList(), d.posterPath(),
                images.url(d.posterPath(), Kind.POSTER, Size.CARD), d.backdropPath(),
                images.url(d.backdropPath(), Kind.BACKDROP, Size.HERO));
    }

    private TvSummary summary(TvDetails d) {
        return new TvSummary(d.id(), MediaType.TV, d.title(), d.originalTitle(), d.overview(), d.releaseDate(),
                d.year(), d.rating(), d.voteCount(), 0, d.genres().stream().map(Genre::id).toList(),
                d.genres().stream().map(Genre::name).toList(), d.posterPath(),
                images.url(d.posterPath(), Kind.POSTER, Size.CARD), d.backdropPath(),
                images.url(d.backdropPath(), Kind.BACKDROP, Size.HERO));
    }

    private static WatchlistItem toDto(com.movieai.entity.WatchlistItem item, MediaSummary media) {
        return new WatchlistItem(item.getId(), item.getMediaType(), item.getTmdbId(), item.getCreatedAt(), media);
    }
}
