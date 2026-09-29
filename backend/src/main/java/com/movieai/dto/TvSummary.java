package com.movieai.dto;

import java.time.LocalDate;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Card-level TV show information. releaseDate is the first air date.")
public record TvSummary(
        @Schema(example = "1399") long id,
        @Schema(allowableValues = "tv") MediaType mediaType,
        @Schema(example = "Shōgun") String title,
        String originalTitle,
        String overview,
        @Schema(nullable = true) LocalDate releaseDate,
        @Schema(nullable = true) Integer year,
        @Schema(nullable = true) Double rating,
        int voteCount,
        double popularity,
        List<Integer> genreIds,
        List<String> genres,
        @Schema(nullable = true) String posterPath,
        @Schema(nullable = true) String posterUrl,
        @Schema(nullable = true) String backdropPath,
        @Schema(nullable = true) String backdropUrl)
        implements MediaSummary {
}
