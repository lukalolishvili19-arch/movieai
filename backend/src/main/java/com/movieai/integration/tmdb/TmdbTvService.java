package com.movieai.integration.tmdb;

import java.util.List;
import java.util.Map;

import com.movieai.config.CacheNames;
import com.movieai.dto.CrewMember;
import com.movieai.dto.Genre;
import com.movieai.dto.MediaImages;
import com.movieai.dto.MediaType;
import com.movieai.dto.PagedResponse;
import com.movieai.dto.TvDetails;
import com.movieai.dto.TvSummary;
import com.movieai.dto.VideoAsset;
import com.movieai.dto.WatchProviders;
import com.movieai.exception.ErrorCode;
import com.movieai.exception.ResourceNotFoundException;
import com.movieai.integration.tmdb.model.TmdbModels;
import com.movieai.mapper.TmdbDtoMapper;
import com.movieai.service.cache.ResilientCache;
import com.movieai.service.catalog.DiscoverQuery;
import com.movieai.service.catalog.TimeWindow;
import com.movieai.service.catalog.TvCatalog;
import com.movieai.service.image.ImageUrlResolver.Kind;
import com.movieai.service.image.ImageUrlResolver.Size;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

@Service
public class TmdbTvService implements TvCatalog {

    private static final ParameterizedTypeReference<TmdbModels.Page<TmdbModels.Tv>> TV_PAGE =
            new ParameterizedTypeReference<>() {
            };
    private static final String DETAIL_APPENDS = "credits,videos,images,recommendations,similar,watch/providers";
    private static final int MAX_SIMILAR = 12;

    private final TmdbClient client;
    private final ResilientCache cache;
    private final TmdbDtoMapper mapper;
    private final TmdbGenreService genres;

    public TmdbTvService(TmdbClient client, ResilientCache cache, TmdbDtoMapper mapper, TmdbGenreService genres) {
        this.client = client;
        this.cache = cache;
        this.mapper = mapper;
        this.genres = genres;
    }

    @Override
    public PagedResponse<TvSummary> trending(TimeWindow window, int page) {
        return list(CacheNames.TRENDING, "/trending/tv/" + window.value(), page);
    }

    @Override
    public PagedResponse<TvSummary> popular(int page) {
        return list(CacheNames.LISTS, "/tv/popular", page);
    }

    @Override
    public PagedResponse<TvSummary> airingToday(int page) {
        return list(CacheNames.LISTS, "/tv/airing_today", page);
    }

    @Override
    public PagedResponse<TvSummary> onTheAir(int page) {
        return list(CacheNames.LISTS, "/tv/on_the_air", page);
    }

    @Override
    public PagedResponse<TvSummary> topRated(int page) {
        return list(CacheNames.LISTS, "/tv/top_rated", page);
    }

    @Override
    public PagedResponse<TvSummary> discover(DiscoverQuery query) {
        return cache.get(CacheNames.DISCOVER, "tv:" + query.cacheKey(), () -> mapper.tvPage(
                client.get("/discover/tv", TmdbDiscoverParams.tv(query), TV_PAGE), genres.tvGenreNames()));
    }

    @Override
    public PagedResponse<TvSummary> search(String query, int page) {
        String normalized = query.trim();
        return cache.get(CacheNames.SEARCH, "tv:" + normalized.toLowerCase() + ":" + page, () -> mapper.tvPage(
                client.get("/search/tv", Map.of("query", normalized, "page", page, "include_adult", "false"), TV_PAGE),
                genres.tvGenreNames()));
    }

    @Override
    public TvDetails details(long id, String region) {
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
        return genres.tvGenres();
    }

    private PagedResponse<TvSummary> list(String cacheName, String path, int page) {
        return cache.get(cacheName, path + ":" + page, () -> mapper.tvPage(
                client.get(path, Map.of("page", page), TV_PAGE), genres.tvGenreNames()));
    }

    private Bundle bundle(long id) {
        return cache.get(CacheNames.DETAILS, "tv:" + id, () -> {
            TmdbModels.TvDetails raw;
            try {
                raw = client.get("/tv/" + id, Map.of(
                        "append_to_response", DETAIL_APPENDS,
                        "include_image_language", "en,null"), TmdbModels.TvDetails.class);
            } catch (TmdbNotFoundException e) {
                throw new ResourceNotFoundException(ErrorCode.TV_NOT_FOUND);
            }
            return toBundle(raw);
        });
    }

    private Bundle toBundle(TmdbModels.TvDetails raw) {
        Map<Integer, String> genreNames = genres.tvGenreNames();
        List<VideoAsset> videos = mapper.videos(raw.videos());
        TmdbModels.Page<TmdbModels.Tv> related = raw.recommendations() != null
                && raw.recommendations().results() != null && !raw.recommendations().results().isEmpty()
                ? raw.recommendations() : raw.similar();
        List<TvSummary> similar = related == null ? List.of()
                : mapper.tvPage(related, genreNames).results().stream().limit(MAX_SIMILAR).toList();
        var firstAir = TmdbDtoMapper.parseDate(raw.firstAirDate());

        List<TvDetails.Network> networks = raw.networks() == null ? List.of() : raw.networks().stream()
                .map(n -> new TvDetails.Network(n.id(), n.name(), mapper.imageUrl(n.logoPath(), Kind.LOGO, Size.CARD)))
                .toList();
        List<CrewMember> createdBy = raw.createdBy() == null ? List.of() : raw.createdBy().stream()
                .map(c -> new CrewMember(c.id(), c.name(), "Creator", "Writing",
                        mapper.imageUrl(c.profilePath(), Kind.PROFILE, Size.CARD)))
                .toList();
        List<TvDetails.Season> seasons = raw.seasons() == null ? List.of() : raw.seasons().stream()
                .map(s -> new TvDetails.Season(s.id(), s.seasonNumber(), s.name(),
                        s.episodeCount() == null ? 0 : s.episodeCount(), TmdbDtoMapper.parseDate(s.airDate()),
                        TmdbDtoMapper.blankToNull(s.overview()), mapper.imageUrl(s.posterPath(), Kind.POSTER, Size.THUMB)))
                .toList();

        TvDetails details = new TvDetails(
                raw.id(), MediaType.TV, raw.name(), TmdbDtoMapper.blankToNull(raw.originalName()),
                TmdbDtoMapper.blankToNull(raw.originalLanguage()), TmdbDtoMapper.blankToNull(raw.tagline()),
                TmdbDtoMapper.blankToNull(raw.overview()), firstAir, TmdbDtoMapper.parseDate(raw.lastAirDate()),
                TmdbDtoMapper.year(firstAir), TmdbDtoMapper.rating(raw.voteAverage(), raw.voteCount()),
                raw.voteCount() == null ? 0 : raw.voteCount(), TmdbDtoMapper.blankToNull(raw.status()),
                Boolean.TRUE.equals(raw.inProduction()), raw.numberOfSeasons(), raw.numberOfEpisodes(),
                raw.episodeRunTime() == null ? List.of() : raw.episodeRunTime(), mapper.genres(raw.genres()), networks,
                createdBy, raw.posterPath(), raw.backdropPath(), mapper.imageUrl(raw.posterPath(), Kind.POSTER, Size.DETAIL),
                mapper.imageUrl(raw.backdropPath(), Kind.BACKDROP, Size.HERO), TmdbDtoMapper.blankToNull(raw.homepage()),
                seasons, mapper.cast(raw.credits()), mapper.mediaImages(raw.images()), videos, mapper.trailer(videos),
                similar, null);
        return new Bundle(details, mapper.watchProviders(raw.watchProviders()));
    }

    record Bundle(TvDetails details, Map<String, WatchProviders> providers) {
    }
}
