package com.movieai.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record ImageAsset(
        @Schema(description = "TMDB file path, e.g. /abc123.jpg") String filePath,
        @Schema(description = "Display-sized URL") String url,
        @Schema(description = "Thumbnail-sized URL") String thumbnailUrl,
        int width,
        int height,
        double aspectRatio,
        @Schema(allowableValues = {"poster", "backdrop", "profile", "logo", "still"}) String type) {
}
