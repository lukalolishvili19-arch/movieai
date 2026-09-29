package com.movieai.dto;

import java.time.LocalDate;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

public record PersonCredit(
        @Schema(description = "TMDB movie or TV id") long id,
        MediaType mediaType,
        String title,
        @Schema(nullable = true, description = "Character(s) played, for cast credits") String character,
        @Schema(nullable = true, description = "Job(s), for crew-only credits") String job,
        @Schema(allowableValues = {"cast", "crew"}) String creditType,
        @Schema(nullable = true) LocalDate releaseDate,
        @Schema(nullable = true) Integer year,
        @Schema(nullable = true) Double rating,
        int voteCount,
        double popularity,
        List<String> genres,
        @Schema(nullable = true) String posterUrl,
        @Schema(nullable = true) Integer episodeCount) {
}
