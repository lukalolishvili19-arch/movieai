package com.movieai.dto;

import java.time.LocalDate;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Full TV show details including real season data, credits, media and watch providers.")
public record TvDetails(
        long id,
        @Schema(allowableValues = "tv") MediaType mediaType,
        String title,
        @Schema(nullable = true) String originalTitle,
        @Schema(nullable = true) String originalLanguage,
        @Schema(nullable = true) String tagline,
        @Schema(nullable = true) String overview,
        @Schema(nullable = true, description = "First air date") LocalDate releaseDate,
        @Schema(nullable = true) LocalDate lastAirDate,
        @Schema(nullable = true) Integer year,
        @Schema(nullable = true) Double rating,
        int voteCount,
        @Schema(nullable = true, example = "Returning Series") String status,
        boolean inProduction,
        @Schema(nullable = true) Integer numberOfSeasons,
        @Schema(nullable = true) Integer numberOfEpisodes,
        List<Integer> episodeRunTime,
        List<Genre> genres,
        List<Network> networks,
        List<CrewMember> createdBy,
        @Schema(nullable = true) String posterPath,
        @Schema(nullable = true) String backdropPath,
        @Schema(nullable = true) String posterUrl,
        @Schema(nullable = true) String backdropUrl,
        @Schema(nullable = true) String homepage,
        List<Season> seasons,
        List<CastMember> cast,
        MediaImages images,
        List<VideoAsset> videos,
        @Schema(nullable = true) VideoAsset trailer,
        List<TvSummary> similar,
        @Schema(nullable = true) WatchProviders watchProviders) {

    public TvDetails withWatchProviders(WatchProviders providers) {
        return new TvDetails(id, mediaType, title, originalTitle, originalLanguage, tagline, overview, releaseDate,
                lastAirDate, year, rating, voteCount, status, inProduction, numberOfSeasons, numberOfEpisodes,
                episodeRunTime, genres, networks, createdBy, posterPath, backdropPath, posterUrl, backdropUrl, homepage, seasons, cast, images,
                videos, trailer, similar, providers);
    }

    public record Network(int id, String name, @Schema(nullable = true) String logoUrl) {
    }

    public record Season(
            long id,
            int seasonNumber,
            String name,
            int episodeCount,
            @Schema(nullable = true) LocalDate airDate,
            @Schema(nullable = true) String overview,
            @Schema(nullable = true) String posterUrl) {
    }
}
