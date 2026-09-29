package com.movieai.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record CrewMember(
        @Schema(description = "TMDB person id") long id,
        String name,
        @Schema(example = "Director") String job,
        @Schema(example = "Directing") String department,
        @Schema(nullable = true) String profileUrl) {
}
