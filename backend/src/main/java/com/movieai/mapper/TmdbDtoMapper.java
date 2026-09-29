package com.movieai.mapper;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.movieai.dto.CastMember;
import com.movieai.dto.CrewMember;
import com.movieai.dto.Genre;
import com.movieai.dto.ImageAsset;
import com.movieai.dto.MediaImages;
import com.movieai.dto.MediaType;
import com.movieai.dto.MovieSummary;
import com.movieai.dto.PagedResponse;
import com.movieai.dto.PersonCredit;
import com.movieai.dto.PersonCredits;
import com.movieai.dto.PersonSummary;
import com.movieai.dto.TvSummary;
import com.movieai.dto.VideoAsset;
import com.movieai.dto.WatchProvider;
import com.movieai.dto.WatchProviders;
import com.movieai.integration.tmdb.model.TmdbModels;
import com.movieai.service.image.ImageUrlResolver;
import com.movieai.service.image.ImageUrlResolver.Kind;
import com.movieai.service.image.ImageUrlResolver.Size;

import org.springframework.stereotype.Component;

/**
 * Converts raw TMDB models into application DTOs. Never invents values: anything TMDB does not
 * provide becomes {@code null} or an empty list.
 */
@Component
public class TmdbDtoMapper {

    /** TMDB caps list endpoints at 500 pages. */
    public static final int MAX_PAGES = 500;
    private static final int MAX_IMAGES = 24;
    private static final int MAX_CAST = 20;
    private static final Set<Integer> NON_SCRIPTED_TV_GENRES = Set.of(10763, 10767); // News, Talk
    private static final Set<String> KEY_CREW_JOBS = Set.of(
            "Screenplay", "Writer", "Novel", "Story", "Producer", "Original Music Composer", "Director of Photography");

    private final ImageUrlResolver images;

    public TmdbDtoMapper(ImageUrlResolver images) {
        this.images = images;
    }

    // ── Summaries ────────────────────────────────────────────────────────────

    public MovieSummary movieSummary(TmdbModels.Movie m, Map<Integer, String> genreNames) {
        LocalDate date = parseDate(m.releaseDate());
        List<Integer> genreIds = orEmpty(m.genreIds());
        return new MovieSummary(m.id(), MediaType.MOVIE, m.title(), m.originalTitle(), blankToNull(m.overview()), date,
                year(date), rating(m.voteAverage(), m.voteCount()), orZero(m.voteCount()), orZero(m.popularity()),
                genreIds, genreNames(genreIds, genreNames), m.posterPath(),
                images.url(m.posterPath(), Kind.POSTER, Size.CARD), m.backdropPath(),
                images.url(m.backdropPath(), Kind.BACKDROP, Size.HERO));
    }

    public TvSummary tvSummary(TmdbModels.Tv t, Map<Integer, String> genreNames) {
        LocalDate date = parseDate(t.firstAirDate());
        List<Integer> genreIds = orEmpty(t.genreIds());
        return new TvSummary(t.id(), MediaType.TV, t.name(), t.originalName(), blankToNull(t.overview()), date,
                year(date), rating(t.voteAverage(), t.voteCount()), orZero(t.voteCount()), orZero(t.popularity()),
                genreIds, genreNames(genreIds, genreNames), t.posterPath(),
                images.url(t.posterPath(), Kind.POSTER, Size.CARD), t.backdropPath(),
                images.url(t.backdropPath(), Kind.BACKDROP, Size.HERO));
    }

    public PersonSummary personSummary(TmdbModels.Person p) {
        List<String> knownFor = orEmpty(p.knownFor()).stream()
                .map(k -> k.title() != null ? k.title() : k.name())
                .filter(Objects::nonNull)
                .toList();
        return new PersonSummary(p.id(), p.name(), blankToNull(p.knownForDepartment()), knownFor,
                orZero(p.popularity()), p.profilePath(), images.url(p.profilePath(), Kind.PROFILE, Size.CARD));
    }

    public PagedResponse<MovieSummary> moviePage(TmdbModels.Page<TmdbModels.Movie> page, Map<Integer, String> genres) {
        List<MovieSummary> results = orEmpty(page.results()).stream()
                .filter(m -> !Boolean.TRUE.equals(m.adult()))
                .map(m -> movieSummary(m, genres))
                .toList();
        return new PagedResponse<>(results, Math.max(1, page.page()), Math.min(page.totalPages(), MAX_PAGES),
                page.totalResults());
    }

    public PagedResponse<TvSummary> tvPage(TmdbModels.Page<TmdbModels.Tv> page, Map<Integer, String> genres) {
        List<TvSummary> results = orEmpty(page.results()).stream()
                .filter(t -> !Boolean.TRUE.equals(t.adult()))
                .map(t -> tvSummary(t, genres))
                .toList();
        return new PagedResponse<>(results, Math.max(1, page.page()), Math.min(page.totalPages(), MAX_PAGES),
                page.totalResults());
    }

    public PagedResponse<PersonSummary> personPage(TmdbModels.Page<TmdbModels.Person> page) {
        List<PersonSummary> results = orEmpty(page.results()).stream()
                .filter(p -> !Boolean.TRUE.equals(p.adult()))
                .map(this::personSummary)
                .toList();
        return new PagedResponse<>(results, Math.max(1, page.page()), Math.min(page.totalPages(), MAX_PAGES),
                page.totalResults());
    }

    // ── Detail building blocks ───────────────────────────────────────────────

    public List<Genre> genres(List<TmdbModels.GenreItem> items) {
        return orEmpty(items).stream().map(g -> new Genre(g.id(), g.name())).toList();
    }

    public List<CastMember> cast(TmdbModels.Credits credits) {
        if (credits == null) {
            return List.of();
        }
        return orEmpty(credits.cast()).stream()
                .sorted(Comparator.comparingInt(c -> c.order() == null ? Integer.MAX_VALUE : c.order()))
                .limit(MAX_CAST)
                .map(c -> new CastMember(c.id(), c.name(), blankToNull(c.character()),
                        c.order() == null ? 0 : c.order(), images.url(c.profilePath(), Kind.PROFILE, Size.CARD)))
                .toList();
    }

    public List<CrewMember> crewWithJob(TmdbModels.Credits credits, String job) {
        if (credits == null) {
            return List.of();
        }
        return orEmpty(credits.crew()).stream()
                .filter(c -> job.equals(c.job()))
                .map(this::crewMember)
                .toList();
    }

    public List<CrewMember> keyCrew(TmdbModels.Credits credits) {
        if (credits == null) {
            return List.of();
        }
        return orEmpty(credits.crew()).stream()
                .filter(c -> KEY_CREW_JOBS.contains(c.job()))
                .limit(12)
                .map(this::crewMember)
                .toList();
    }

    public CrewMember crewMember(TmdbModels.Crew c) {
        return new CrewMember(c.id(), c.name(), c.job(), c.department(),
                images.url(c.profilePath(), Kind.PROFILE, Size.CARD));
    }

    public MediaImages mediaImages(TmdbModels.Images raw) {
        if (raw == null) {
            return MediaImages.empty();
        }
        return new MediaImages(
                imageAssets(raw.backdrops(), Kind.BACKDROP, "backdrop", Size.CARD, Size.THUMB),
                imageAssets(raw.posters(), Kind.POSTER, "poster", Size.DETAIL, Size.CARD),
                imageAssets(raw.logos(), Kind.LOGO, "logo", Size.HERO, Size.DETAIL));
    }

    public List<ImageAsset> profileImages(TmdbModels.Images raw) {
        if (raw == null) {
            return List.of();
        }
        return imageAssets(raw.profiles(), Kind.PROFILE, "profile", Size.DETAIL, Size.CARD);
    }

    private List<ImageAsset> imageAssets(List<TmdbModels.Image> list, Kind kind, String type, Size display, Size thumb) {
        return orEmpty(list).stream()
                .filter(i -> i.filePath() != null)
                .limit(MAX_IMAGES)
                .map(i -> new ImageAsset(i.filePath(), images.url(i.filePath(), kind, display),
                        images.url(i.filePath(), kind, thumb), orZero(i.width()), orZero(i.height()),
                        i.aspectRatio() == null ? 0 : i.aspectRatio(), type))
                .toList();
    }

    public List<VideoAsset> videos(TmdbModels.Videos raw) {
        if (raw == null) {
            return List.of();
        }
        return orEmpty(raw.results()).stream()
                .filter(v -> v.key() != null && v.site() != null)
                .sorted(Comparator.comparingInt(TmdbDtoMapper::videoRank)
                        .thenComparing(v -> v.publishedAt() == null ? "" : v.publishedAt(), Comparator.reverseOrder()))
                .map(this::video)
                .toList();
    }

    /** Best official trailer: YouTube only (playable in the in-app modal). */
    public VideoAsset trailer(List<VideoAsset> videos) {
        return videos.stream()
                .filter(v -> "YouTube".equalsIgnoreCase(v.site()))
                .filter(v -> "Trailer".equalsIgnoreCase(v.type()) || "Teaser".equalsIgnoreCase(v.type()))
                .min(Comparator.comparingInt((VideoAsset v) -> "Trailer".equalsIgnoreCase(v.type()) ? 0 : 1)
                        .thenComparingInt(v -> v.official() ? 0 : 1)
                        .thenComparingInt(v -> v.name() != null && v.name().toLowerCase().contains("official trailer") ? 0 : 1))
                .orElse(null);
    }

    private VideoAsset video(TmdbModels.Video v) {
        boolean youtube = "YouTube".equalsIgnoreCase(v.site());
        boolean vimeo = "Vimeo".equalsIgnoreCase(v.site());
        String watchUrl = youtube ? "https://www.youtube.com/watch?v=" + v.key()
                : vimeo ? "https://vimeo.com/" + v.key() : null;
        String embedUrl = youtube ? "https://www.youtube-nocookie.com/embed/" + v.key()
                : vimeo ? "https://player.vimeo.com/video/" + v.key() : null;
        String thumb = youtube ? "https://i.ytimg.com/vi/" + v.key() + "/hqdefault.jpg" : null;
        return new VideoAsset(v.id(), v.key(), v.name(), v.site(), v.type(), Boolean.TRUE.equals(v.official()),
                parseInstant(v.publishedAt()), watchUrl, embedUrl, thumb);
    }

    private static int videoRank(TmdbModels.Video v) {
        int typeRank = switch (v.type() == null ? "" : v.type()) {
            case "Trailer" -> 0;
            case "Teaser" -> 1;
            case "Clip" -> 2;
            case "Featurette" -> 3;
            case "Behind the Scenes" -> 4;
            default -> 5;
        };
        return typeRank * 2 + (Boolean.TRUE.equals(v.official()) ? 0 : 1);
    }

    public Map<String, WatchProviders> watchProviders(TmdbModels.WatchProviderResults raw) {
        if (raw == null || raw.results() == null) {
            return Map.of();
        }
        Map<String, WatchProviders> byRegion = new LinkedHashMap<>();
        raw.results().forEach((region, p) -> byRegion.put(region.toUpperCase(), new WatchProviders(
                region.toUpperCase(), p.link(), providers(p.flatrate()), providers(p.free()), providers(p.ads()),
                providers(p.rent()), providers(p.buy()), "JustWatch")));
        return byRegion;
    }

    private List<WatchProvider> providers(List<TmdbModels.Provider> list) {
        return orEmpty(list).stream()
                .sorted(Comparator.comparingInt(p -> p.displayPriority() == null ? Integer.MAX_VALUE : p.displayPriority()))
                .map(p -> new WatchProvider(p.providerId(), p.providerName(),
                        images.url(p.logoPath(), Kind.LOGO, Size.CARD),
                        p.displayPriority() == null ? 0 : p.displayPriority()))
                .toList();
    }

    // ── Person credits ───────────────────────────────────────────────────────

    public PersonCredits personCredits(TmdbModels.CombinedCredits raw, Map<Integer, String> movieGenres,
                                       Map<Integer, String> tvGenres) {
        if (raw == null) {
            return new PersonCredits(List.of(), List.of());
        }
        Map<String, PersonCredit> merged = new LinkedHashMap<>();
        Map<String, List<String>> characters = new LinkedHashMap<>();
        Map<String, List<String>> jobs = new LinkedHashMap<>();
        Map<String, Integer> episodes = new LinkedHashMap<>();

        for (TmdbModels.PersonCredit c : orEmpty(raw.cast())) {
            if (!include(c)) {
                continue;
            }
            String key = c.mediaType() + ":" + c.id();
            merged.putIfAbsent(key, credit(c, "cast", movieGenres, tvGenres));
            if (c.character() != null && !c.character().isBlank()) {
                characters.computeIfAbsent(key, k -> new ArrayList<>()).add(c.character());
            }
            if (c.episodeCount() != null) {
                episodes.merge(key, c.episodeCount(), Integer::sum);
            }
        }
        for (TmdbModels.PersonCredit c : orEmpty(raw.crew())) {
            if (!include(c)) {
                continue;
            }
            String key = c.mediaType() + ":" + c.id();
            merged.putIfAbsent(key, credit(c, "crew", movieGenres, tvGenres));
            if (c.job() != null && !c.job().isBlank()) {
                jobs.computeIfAbsent(key, k -> new ArrayList<>()).add(c.job());
            }
        }

        List<PersonCredit> all = merged.entrySet().stream().map(e -> {
            PersonCredit base = e.getValue();
            String character = join(characters.get(e.getKey()));
            String job = join(jobs.get(e.getKey()));
            Integer episodeCount = base.mediaType() == MediaType.TV ? episodes.get(e.getKey()) : null;
            return new PersonCredit(base.id(), base.mediaType(), base.title(), character, job, base.creditType(),
                    base.releaseDate(), base.year(), base.rating(), base.voteCount(), base.popularity(), base.genres(),
                    base.posterUrl(), episodeCount);
        }).sorted(Comparator.comparingDouble(PersonCredit::popularity).reversed()).toList();

        return new PersonCredits(
                all.stream().filter(c -> c.mediaType() == MediaType.MOVIE).toList(),
                all.stream().filter(c -> c.mediaType() == MediaType.TV).toList());
    }

    private static boolean include(TmdbModels.PersonCredit c) {
        if (Boolean.TRUE.equals(c.adult())) {
            return false;
        }
        if ("tv".equals(c.mediaType())) {
            return c.genreIds() == null || c.genreIds().stream().noneMatch(NON_SCRIPTED_TV_GENRES::contains);
        }
        return "movie".equals(c.mediaType());
    }

    private PersonCredit credit(TmdbModels.PersonCredit c, String creditType, Map<Integer, String> movieGenres,
                                Map<Integer, String> tvGenres) {
        boolean movie = "movie".equals(c.mediaType());
        LocalDate date = parseDate(movie ? c.releaseDate() : c.firstAirDate());
        List<Integer> genreIds = orEmpty(c.genreIds());
        return new PersonCredit(c.id(), movie ? MediaType.MOVIE : MediaType.TV, movie ? c.title() : c.name(), null,
                null, creditType, date, year(date), rating(c.voteAverage(), c.voteCount()), orZero(c.voteCount()),
                orZero(c.popularity()), genreNames(genreIds, movie ? movieGenres : tvGenres),
                images.url(c.posterPath(), Kind.POSTER, Size.CARD), null);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    public String imageUrl(String path, Kind kind, Size size) {
        return images.url(path, kind, size);
    }

    public static LocalDate parseDate(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(raw.trim());
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private static Instant parseInstant(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return Instant.parse(raw.trim());
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    public static Integer year(LocalDate date) {
        return date == null ? null : date.getYear();
    }

    /** A vote average without votes is not a rating; report it as unavailable. */
    public static Double rating(Double voteAverage, Integer voteCount) {
        if (voteAverage == null || voteCount == null || voteCount == 0 || voteAverage <= 0) {
            return null;
        }
        return Math.round(voteAverage * 10.0) / 10.0;
    }

    private static List<String> genreNames(List<Integer> ids, Map<Integer, String> names) {
        return ids.stream().map(names::get).filter(Objects::nonNull).toList();
    }

    public static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private static String join(Collection<String> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }
        return values.stream().distinct().collect(Collectors.joining(", "));
    }

    private static <T> List<T> orEmpty(List<T> list) {
        return list == null ? List.of() : list;
    }

    private static int orZero(Integer value) {
        return value == null ? 0 : value;
    }

    private static double orZero(Double value) {
        return value == null ? 0 : value;
    }
}
