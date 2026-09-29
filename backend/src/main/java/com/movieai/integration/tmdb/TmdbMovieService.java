package com.movieai.integration.tmdb;

import java.util.List;
import java.util.Map;

import com.movieai.config.CacheNames;
import com.movieai.dto.Genre;
import com.movieai.dto.MediaImages;
import com.movieai.dto.MediaType;
import com.movieai.dto.MovieDetails;
import com.movieai.dto.MovieSummary;
import com.movieai.dto.PagedResponse;
import com.movieai.dto.VideoAsset;
import com.movieai.dto.WatchProviders;
import com.movieai.exception.ErrorCode;
import com.movieai.exception.ResourceNotFoundException;
import com.movieai.integration.tmdb.model.TmdbModels;
import com.movieai.mapper.TmdbDtoMapper;
import com.movieai.service.cache.ResilientCache;
import com.movieai.service.catalog.DiscoverQuery;
import com.movieai.service.catalog.MovieCatalog;
import com.movieai.service.catalog.TimeWindow;
import com.movieai.service.image.ImageUrlResolver.Kind;
import com.movieai.service.image.ImageUrlResolver.Size;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

@Service
public class TmdbMovieService implements MovieCatalog {

    private static final ParameterizedTypeReference<TmdbModels.Page<TmdbModels.Movie>> MOVIE_PAGE =
            new ParameterizedTypeReference<>() {
            };
    private static final String DETAIL_APPENDS = "credits,videos,images,recommendations,similar,watch/providers";
    private static final int MAX_SIMILAR = 12;

    private final TmdbClient client;
    private final ResilientCache cache;
    private final TmdbDtoMapper mapper;
    private final TmdbGenreService genres;

    public TmdbMovieService(TmdbClient client, ResilientCache cache, TmdbDtoMapper mapper, TmdbGenreService genres) {
        this.client = client;
        this.cache = cache;
        this.mapper = mapper;
        this.genres = genres;
    }

    @Override
    public PagedResponse<MovieSummary> trending(TimeWindow window, int page) {
        return list(CacheNames.TRENDING, "/trending/movie/" + window.value(), page);
    }

    @Override
    public PagedResponse<MovieSummary> popular(int page) {
        return list(CacheNames.LISTS, "/movie/popular", page);
    }

    @Override
    public PagedResponse<MovieSummary> nowPlaying(int page) {
        return list(CacheNames.LISTS, "/movie/now_playing", page);
    }

    @Override
    public PagedResponse<MovieSummary> upcoming(int page) {
        return list(CacheNames.LISTS, "/movie/upcoming", page);
    }

    @Override
    public PagedResponse<MovieSummary> topRated(int page) {
        return list(CacheNames.LISTS, "/movie/top_rated", page);
    }

    @Override
    public PagedResponse<MovieSummary> discover(DiscoverQuery query) {
        return cache.get(CacheNames.DISCOVER, "movie:" + query.cacheKey(), () -> mapper.moviePage(
                client.get("/discover/movie", TmdbDiscoverParams.movie(query), MOVIE_PAGE), genres.movieGenreNames()));
    }

    @Override
    public PagedResponse<MovieSummary> search(String query, int page) {
        String normalized = query.trim();
        return cache.get(CacheNames.SEARCH, "movie:" + normalized.toLowerCase() + ":" + page, () -> mapper.moviePage(
                client.get("/search/movie", Map.of("query", normalized, "page", page, "include_adult", "false"),
                        MOVIE_PAGE), genres.movieGenreNames()));
    }

    @Override
    public MovieDetails details(long id, String region) {
        Bundle bundle = bundle(id);
        return bundle.details().withWatchProviders(bundle.providers().getOrDefault(region, WatchProviders.none(region)));
    }

    @Override
    public MediaImages images(long id) {
        return bundle(id).details().images();
    }

    @Override
    public List<VideoAsset> videos(long id) {
        return bundle(id).details().videos();
    }

    @Override
    public WatchProviders watchProviders(long id, String region) {
        return bundle(id).providers().getOrDefault(region, WatchProviders.none(region));
    }

    @Override
    public List<Genre> genres() {
        return genres.movieGenres();
    }

    private PagedResponse<MovieSummary> list(String cacheName, String path, int page) {
        return cache.get(cacheName, path + ":" + page, () -> mapper.moviePage(
                client.get(path, Map.of("page", page), MOVIE_PAGE), genres.movieGenreNames()));
    }

    /**
     * One TMDB request (with append_to_response) serves details, credits, images, videos,
     * recommendations and watch providers for every region.
     */
    private Bundle bundle(long id) {
        return cache.get(CacheNames.DETAILS, "movie:" + id, () -> {
            TmdbModels.MovieDetails raw;
            try {
                raw = client.get("/movie/" + id, Map.of(
                        "append_to_response", DETAIL_APPENDS,
                        "include_image_language", "en,null"), TmdbModels.MovieDetails.class);
            } catch (TmdbNotFoundException e) {
                throw new ResourceNotFoundException(ErrorCode.MOVIE_NOT_FOUND);
            }
            return toBundle(raw);
        });
    }

    private Bundle toBundle(TmdbModels.MovieDetails raw) {
        Map<Integer, String> genreNames = genres.movieGenreNames();
        List<VideoAsset> videos = mapper.videos(raw.videos());
        TmdbModels.Page<TmdbModels.Movie> related = raw.recommendations() != null
                && raw.recommendations().results() != null && !raw.recommendations().results().isEmpty()
                ? raw.recommendations() : raw.similar();
        List<MovieSummary> similar = related == null ? List.of()
                : mapper.moviePage(related, genreNames).results().stream().limit(MAX_SIMILAR).toList();
        var releaseDate = TmdbDtoMapper.parseDate(raw.releaseDate());

        MovieDetails details = new MovieDetails(
                raw.id(), MediaType.MOVIE, raw.title(), TmdbDtoMapper.blankToNull(raw.originalTitle()),
                TmdbDtoMapper.blankToNull(raw.originalLanguage()), TmdbDtoMapper.blankToNull(raw.tagline()),
                TmdbDtoMapper.blankToNull(raw.overview()), releaseDate, TmdbDtoMapper.year(releaseDate),
                raw.runtime() == null || raw.runtime() == 0 ? null : raw.runtime(),
                TmdbDtoMapper.rating(raw.voteAverage(), raw.voteCount()),
                raw.voteCount() == null ? 0 : raw.voteCount(), TmdbDtoMapper.blankToNull(raw.status()),
                mapper.genres(raw.genres()), raw.posterPath(), raw.backdropPath(),
                mapper.imageUrl(raw.posterPath(), Kind.POSTER, Size.DETAIL),
                mapper.imageUrl(raw.backdropPath(), Kind.BACKDROP, Size.HERO),
                TmdbDtoMapper.blankToNull(raw.homepage()), TmdbDtoMapper.blankToNull(raw.imdbId()),
                mapper.crewWithJob(raw.credits(), "Director"), mapper.keyCrew(raw.credits()),
                mapper.cast(raw.credits()), mapper.mediaImages(raw.images()), videos, mapper.trailer(videos),
                similar, null);
        return new Bundle(details, mapper.watchProviders(raw.watchProviders()));
    }

    /** Cached unit: region-independent details plus providers for all regions. */
    record Bundle(MovieDetails details, Map<String, WatchProviders> providers) {
    }
}
