package com.movieai.dto;

import java.time.LocalDate;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Person details. Every field is exactly what TMDB returns; missing values are null or empty.")
public record PersonDetails(
        long id,
        String name,
        @Schema(nullable = true) String biography,
        @Schema(nullable = true) LocalDate birthday,
        @Schema(nullable = true) LocalDate deathday,
        @Schema(nullable = true) String placeOfBirth,
        @Schema(nullable = true) String knownForDepartment,
        List<String> alsoKnownAs,
        double popularity,
        @Schema(nullable = true) String profileUrl,
        @Schema(nullable = true) String homepage,
        @Schema(nullable = true) String imdbId,
        PersonCredits credits,
        List<ImageAsset> images) {
}
