package com.movieai.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record CastMember(
        @Schema(description = "TMDB person id") long id,
        String name,
        @Schema(nullable = true) String character,
        int order,
        @Schema(nullable = true) String profileUrl) {
}
