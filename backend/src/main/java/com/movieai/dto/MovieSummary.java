package com.movieai.dto;

import java.time.LocalDate;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Card-level movie information. Nullable fields are null when TMDB has no value.")
public record MovieSummary(
        @Schema(example = "693134") long id,
        @Schema(allowableValues = "movie") MediaType mediaType,
        @Schema(example = "Dune: Part Two") String title,
        String originalTitle,
        String overview,
        @Schema(nullable = true) LocalDate releaseDate,
        @Schema(nullable = true, example = "2024") Integer year,
        @Schema(nullable = true, description = "TMDB vote average (0-10); null when there are no votes", example = "8.2") Double rating,
        int voteCount,
        double popularity,
        List<Integer> genreIds,
        List<String> genres,
        @Schema(nullable = true) String posterPath,
        @Schema(nullable = true, description = "Card-sized poster URL") String posterUrl,
        @Schema(nullable = true) String backdropPath,
        @Schema(nullable = true, description = "Large backdrop URL suitable for hero banners") String backdropUrl)
        implements MediaSummary {
}
