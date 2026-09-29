package com.movieai.dto;

import java.util.List;
import java.util.function.Function;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "A single page of results.")
public record PagedResponse<T>(
        List<T> results,
        @Schema(example = "1") int page,
        @Schema(example = "500") int totalPages,
        @Schema(example = "10000") int totalResults) {

    public static <T> PagedResponse<T> empty() {
        return new PagedResponse<>(List.of(), 1, 0, 0);
    }

    public <R> PagedResponse<R> map(Function<T, R> mapper) {
        return new PagedResponse<>(results.stream().map(mapper).toList(), page, totalPages, totalResults);
    }
}
