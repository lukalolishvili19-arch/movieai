package com.movieai.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

public record VideoList(
        List<VideoAsset> results,
        @Schema(nullable = true, description = "Best official trailer, or null when none exists") VideoAsset trailer) {
}
