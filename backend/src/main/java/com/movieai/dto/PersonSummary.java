package com.movieai.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

public record PersonSummary(
        @Schema(example = "1190668") long id,
        @Schema(example = "Timothée Chalamet") String name,
        @Schema(nullable = true, example = "Acting") String knownForDepartment,
        @Schema(description = "Titles this person is best known for, as returned by TMDB") List<String> knownFor,
        double popularity,
        @Schema(nullable = true) String profilePath,
        @Schema(nullable = true, description = "Card-sized profile photo URL") String profileUrl) {
}
