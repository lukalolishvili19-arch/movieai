package com.movieai.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record WatchProvider(
        @Schema(example = "8") int id,
        @Schema(example = "Netflix") String name,
        @Schema(nullable = true) String logoUrl,
        int displayPriority) {
}
