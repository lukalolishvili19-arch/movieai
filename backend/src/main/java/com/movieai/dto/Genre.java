package com.movieai.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record Genre(
        @Schema(example = "878") int id,
        @Schema(example = "Science Fiction") String name) {
}
