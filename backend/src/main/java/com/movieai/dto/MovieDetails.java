package com.movieai.dto;

import java.time.LocalDate;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Full movie details including credits, media and regional watch providers.")
public record MovieDetails(
        long id,
        @Schema(allowableValues = "movie") MediaType mediaType,
        String title,
        @Schema(nullable = true) String originalTitle,
        @Schema(nullable = true) String originalLanguage,
        @Schema(nullable = true) String tagline,
        @Schema(nullable = true) String overview,
        @Schema(nullable = true) LocalDate releaseDate,
        @Schema(nullable = true) Integer year,
        @Schema(nullable = true, description = "Runtime in minutes") Integer runtime,
        @Schema(nullable = true) Double rating,
        int voteCount,
        @Schema(nullable = true, example = "Released") String status,
        List<Genre> genres,
        @Schema(nullable = true) String posterPath,
        @Schema(nullable = true) String backdropPath,
        @Schema(nullable = true) String posterUrl,
        @Schema(nullable = true) String backdropUrl,
        @Schema(nullable = true) String homepage,
        @Schema(nullable = true) String imdbId,
        List<CrewMember> directors,
        @Schema(description = "Key crew (writing, production, music, photography)") List<CrewMember> crew,
        @Schema(description = "Top-billed cast, in billing order") List<CastMember> cast,
        MediaImages images,
        List<VideoAsset> videos,
        @Schema(nullable = true, description = "Best official trailer, if any") VideoAsset trailer,
        List<MovieSummary> similar,
        @Schema(nullable = true) WatchProviders watchProviders) {

    public MovieDetails withWatchProviders(WatchProviders providers) {
        return new MovieDetails(id, mediaType, title, originalTitle, originalLanguage, tagline, overview, releaseDate,
                year, runtime, rating, voteCount, status, genres, posterPath, backdropPath, posterUrl, backdropUrl, homepage, imdbId, directors,
                crew, cast, images, videos, trailer, similar, providers);
    }
}
