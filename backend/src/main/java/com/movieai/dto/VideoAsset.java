package com.movieai.dto;

import java.time.Instant;

import io.swagger.v3.oas.annotations.media.Schema;

public record VideoAsset(
        String id,
        @Schema(description = "Provider video key (YouTube id for YouTube videos)") String key,
        String name,
        @Schema(example = "YouTube") String site,
        @Schema(example = "Trailer") String type,
        boolean official,
        @Schema(nullable = true) Instant publishedAt,
        @Schema(nullable = true, description = "Public watch URL, when the site is supported") String watchUrl,
        @Schema(nullable = true, description = "Embeddable player URL, when the site is supported") String embedUrl,
        @Schema(nullable = true) String thumbnailUrl) {
}
