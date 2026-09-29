package com.movieai.controller;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import com.movieai.dto.MovieSummary;
import com.movieai.dto.PagedResponse;
import com.movieai.dto.PersonSummary;
import com.movieai.dto.SearchResults;
import com.movieai.dto.TvSummary;
import com.movieai.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/search")
@Tag(name = "Search", description = "Search movies, TV shows and people. Rate limited per client.")
public class SearchController {

    private final SearchService search;

    public SearchController(SearchService search) {
        this.search = search;
    }

    @GetMapping
    @Operation(summary = "Search everything", description = "First page of movie, TV and people results in one call.")
    public ResponseEntity<SearchResults> all(
            @Parameter(description = "Search text (1-100 characters)", required = true)
            @RequestParam @Size(min = 1, max = 100) String query) {
        return Params.cached(search.all(Params.query(query)));
    }

    @GetMapping("/movies")
    @Operation(summary = "Search movies")
    public ResponseEntity<PagedResponse<MovieSummary>> movies(
            @RequestParam @Size(min = 1, max = 100) String query,
            @RequestParam(defaultValue = "1") @Min(1) @Max(500) int page) {
        return Params.cached(search.movies(Params.query(query), page));
    }

    @GetMapping("/tv")
    @Operation(summary = "Search TV shows")
    public ResponseEntity<PagedResponse<TvSummary>> tv(
            @RequestParam @Size(min = 1, max = 100) String query,
            @RequestParam(defaultValue = "1") @Min(1) @Max(500) int page) {
        return Params.cached(search.tv(Params.query(query), page));
    }

    @GetMapping("/people")
    @Operation(summary = "Search people")
    public ResponseEntity<PagedResponse<PersonSummary>> people(
            @RequestParam @Size(min = 1, max = 100) String query,
            @RequestParam(defaultValue = "1") @Min(1) @Max(500) int page) {
        return Params.cached(search.people(Params.query(query), page));
    }
}
