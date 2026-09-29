package com.movieai.integration.tmdb.model;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Raw TMDB response shapes. These types never leave the integration layer: services map them
 * to application DTOs. Dates are kept as strings because TMDB returns "" for unknown dates.
 * Field names are camelCase and bound through a SNAKE_CASE ObjectMapper.
 */
public final class TmdbModels {

    private TmdbModels() {
    }

    public record Page<T>(int page, List<T> results, int totalPages, int totalResults) {
    }

    public record Movie(
            long id, String title, String originalTitle, String overview, String releaseDate,
            Double voteAverage, Integer voteCount, Double popularity, List<Integer> genreIds,
            String posterPath, String backdropPath, Boolean adult) {
    }

    public record Tv(
            long id, String name, String originalName, String overview, String firstAirDate,
            Double voteAverage, Integer voteCount, Double popularity, List<Integer> genreIds,
            String posterPath, String backdropPath, Boolean adult) {
    }

    public record KnownFor(long id, String mediaType, String title, String name) {
    }

    public record Person(
            long id, String name, String knownForDepartment, Double popularity, String profilePath,
            List<KnownFor> knownFor, Boolean adult) {
    }

    public record GenreItem(int id, String name) {
    }

    public record GenreList(List<GenreItem> genres) {
    }

    public record Cast(long id, String name, String character, Integer order, String profilePath) {
    }

    public record Crew(long id, String name, String job, String department, String profilePath) {
    }

    public record Credits(List<Cast> cast, List<Crew> crew) {
    }

    public record Image(String filePath, Integer width, Integer height, Double aspectRatio, String iso6391,
                        Double voteAverage) {
    }

    public record Images(List<Image> backdrops, List<Image> posters, List<Image> logos, List<Image> profiles) {
    }

    public record Video(String id, String key, String name, String site, String type, Boolean official,
                        String publishedAt, Integer size, String iso6391) {
    }

    public record Videos(List<Video> results) {
    }

    public record Provider(int providerId, String providerName, String logoPath, Integer displayPriority) {
    }

    public record RegionProviders(String link, List<Provider> flatrate, List<Provider> free, List<Provider> ads,
                                  List<Provider> rent, List<Provider> buy) {
    }

    public record WatchProviderResults(Map<String, RegionProviders> results) {
    }

    public record ProductionItem(int id, String name, String logoPath) {
    }

    public record MovieDetails(
            long id, String title, String originalTitle, String originalLanguage, String tagline, String overview,
            String releaseDate, Integer runtime, Double voteAverage, Integer voteCount, String status,
            List<GenreItem> genres, String posterPath, String backdropPath, String homepage, String imdbId,
            Credits credits, Videos videos, Images images, Page<Movie> similar, Page<Movie> recommendations,
            @JsonProperty("watch/providers") WatchProviderResults watchProviders) {
    }

    public record Season(long id, int seasonNumber, String name, Integer episodeCount, String airDate,
                         String overview, String posterPath) {
    }

    public record CreatedBy(long id, String name, String profilePath) {
    }

    public record TvDetails(
            long id, String name, String originalName, String originalLanguage, String tagline, String overview,
            String firstAirDate, String lastAirDate, Double voteAverage, Integer voteCount, String status,
            Boolean inProduction, Integer numberOfSeasons, Integer numberOfEpisodes, List<Integer> episodeRunTime,
            List<GenreItem> genres, List<ProductionItem> networks, List<CreatedBy> createdBy, String posterPath,
            String backdropPath, String homepage, List<Season> seasons, Credits credits, Credits aggregateCredits,
            Videos videos, Images images, Page<Tv> similar, Page<Tv> recommendations,
            @JsonProperty("watch/providers") WatchProviderResults watchProviders) {
    }

    public record PersonCredit(
            long id, String mediaType, String title, String name, String character, String job, String department,
            String releaseDate, String firstAirDate, Double voteAverage, Integer voteCount, Double popularity,
            List<Integer> genreIds, String posterPath, Integer episodeCount, Boolean adult) {
    }

    public record CombinedCredits(List<PersonCredit> cast, List<PersonCredit> crew) {
    }

    public record PersonDetails(
            long id, String name, String biography, String birthday, String deathday, String placeOfBirth,
            String knownForDepartment, List<String> alsoKnownAs, Double popularity, String profilePath,
            String homepage, String imdbId, CombinedCredits combinedCredits, Images images) {
    }

    public record ImageConfiguration(String secureBaseUrl, List<String> posterSizes, List<String> backdropSizes,
                                     List<String> profileSizes, List<String> logoSizes, List<String> stillSizes) {
    }

    public record Configuration(ImageConfiguration images) {
    }
}
