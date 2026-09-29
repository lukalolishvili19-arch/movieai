package com.movieai.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "First page of movie, TV and people matches for a query.")
public record SearchResults(
        String query,
        PagedResponse<MovieSummary> movies,
        PagedResponse<TvSummary> tv,
        PagedResponse<PersonSummary> people) {
}
