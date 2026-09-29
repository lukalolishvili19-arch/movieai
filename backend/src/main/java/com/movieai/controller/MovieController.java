package com.movieai.controller;

import java.util.List;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import com.movieai.config.MovieAiProperties;
import com.movieai.dto.Genre;
import com.movieai.dto.MediaImages;
import com.movieai.dto.MovieDetails;
import com.movieai.dto.MovieSummary;
import com.movieai.dto.PagedResponse;
import com.movieai.dto.VideoAsset;
import com.movieai.dto.WatchProviders;
import com.movieai.service.browse.BrowseFilters;
import com.movieai.service.browse.BrowseService;
import com.movieai.service.browse.BrowseService.MovieCategory;
import com.movieai.service.browse.RuntimeRange;
import com.movieai.service.catalog.DiscoverQuery.SortOption;
import com.movieai.service.catalog.MovieCatalog;
import com.movieai.service.catalog.TimeWindow;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/movies")
@Tag(name = "Movies", description = "Public movie lists, discovery and details. No authentication required.")
public class MovieController {

    private final MovieCatalog catalog;
    private final BrowseService browse;
    private final String defaultRegion;

    public MovieController(MovieCatalog catalog, BrowseService browse, MovieAiProperties properties) {
        this.catalog = catalog;
        this.browse = browse;
        this.defaultRegion = properties.tmdb().defaultRegion();
    }

    @GetMapping
    @Operation(summary = "Browse movies",
            description = "Paginated movie browsing with server-side filtering. Without filters the curated list for "
                    + "the category is returned; with filters, discovery is used with the category as a constraint.")
    public ResponseEntity<PagedResponse<MovieSummary>> browse(
            @Parameter(schema = @Schema(allowableValues = {"trending", "popular", "top-rated", "now-playing", "upcoming"}))
            @RequestParam(defaultValue = "popular") String category,
            @Parameter(description = "Trending window (category=trending only)", schema = @Schema(allowableValues = {"day", "week"}))
            @RequestParam(defaultValue = "day") String period,
            @Parameter(description = "Genre ids (comma-separated); results match all of them")
            @RequestParam(required = false) @Size(max = 10) List<@Positive Integer> genres,
            @Parameter(description = "Minimum TMDB rating (0-10)")
            @RequestParam(required = false) @DecimalMin("0") @DecimalMax("10") Double minRating,
            @RequestParam(required = false) @Min(1874) @Max(2100) Integer yearFrom,
            @RequestParam(required = false) @Min(1874) @Max(2100) Integer yearTo,
            @Parameter(schema = @Schema(allowableValues = {"any", "under-90", "90-120", "over-120"}))
            @RequestParam(required = false) String runtime,
            @Parameter(schema = @Schema(allowableValues = {"popularity", "rating", "release_date", "title"}))
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "1") @Min(1) @Max(500) int page) {
        Params.yearRange(yearFrom, yearTo);
        MovieCategory cat = Params.parse("category", category,
                v -> MovieCategory.valueOf(v.trim().toUpperCase().replace('-', '_')));
        BrowseFilters filters = new BrowseFilters(genres, minRating, yearFrom, yearTo,
                Params.parse("runtime", runtime, RuntimeRange::fromValue),
                Params.parse("sort", sort, SortOption::fromValue));
        return Params.cached(browse.movies(cat, Params.parse("period", period, TimeWindow::fromValue), filters, page));
    }

    @GetMapping("/trending")
    @Operation(summary = "Trending movies", description = "TMDB trending movies for the given time window.")
    public ResponseEntity<PagedResponse<MovieSummary>> trending(
            @Parameter(schema = @Schema(allowableValues = {"day", "week"})) @RequestParam(defaultValue = "day") String period,
            @RequestParam(defaultValue = "1") @Min(1) @Max(500) int page) {
        return Params.cached(catalog.trending(Params.parse("period", period, TimeWindow::fromValue), page));
    }

    @GetMapping("/popular")
    @Operation(summary = "Popular movies")
    public ResponseEntity<PagedResponse<MovieSummary>> popular(@RequestParam(defaultValue = "1") @Min(1) @Max(500) int page) {
        return Params.cached(catalog.popular(page));
    }

    @GetMapping("/now-playing")
    @Operation(summary = "Movies now playing in theatres")
    public ResponseEntity<PagedResponse<MovieSummary>> nowPlaying(@RequestParam(defaultValue = "1") @Min(1) @Max(500) int page) {
        return Params.cached(catalog.nowPlaying(page));
    }

    @GetMapping("/upcoming")
    @Operation(summary = "Upcoming movies (Coming Soon)")
    public ResponseEntity<PagedResponse<MovieSummary>> upcoming(@RequestParam(defaultValue = "1") @Min(1) @Max(500) int page) {
        return Params.cached(catalog.upcoming(page));
    }

    @GetMapping("/top-rated")
    @Operation(summary = "Top rated movies")
    public ResponseEntity<PagedResponse<MovieSummary>> topRated(@RequestParam(defaultValue = "1") @Min(1) @Max(500) int page) {
        return Params.cached(catalog.topRated(page));
    }

    @GetMapping("/genres")
    @Operation(summary = "Movie genres")
    public ResponseEntity<List<Genre>> genres() {
        return Params.cached(catalog.genres());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Movie details",
            description = "Details with credits, images, videos, best trailer, similar titles and watch providers for "
                    + "the requested region. Returns MOVIE_NOT_FOUND (404) for unknown ids.")
    public ResponseEntity<MovieDetails> details(
            @PathVariable @Positive long id,
            @Parameter(description = "ISO 3166-1 country code for watch providers", example = "US")
            @RequestParam(required = false) @Pattern(regexp = Params.REGION_PATTERN) String region) {
        return Params.cached(catalog.details(id, Params.region(region, defaultRegion)));
    }

    @GetMapping("/{id}/images")
    @Operation(summary = "Movie images (backdrops, posters, logos)")
    public ResponseEntity<MediaImages> images(@PathVariable @Positive long id) {
        return Params.cached(catalog.images(id));
    }

    @GetMapping("/{id}/videos")
    @Operation(summary = "Movie videos", description = "Trailers first, official before unofficial.")
    public ResponseEntity<List<VideoAsset>> videos(@PathVariable @Positive long id) {
        return Params.cached(catalog.videos(id));
    }

    @GetMapping("/{id}/watch-providers")
    @Operation(summary = "Where to watch a movie", description = "Streaming, rental and purchase options. Data by JustWatch.")
    public ResponseEntity<WatchProviders> watchProviders(
            @PathVariable @Positive long id,
            @RequestParam(required = false) @Pattern(regexp = Params.REGION_PATTERN) String region) {
        return Params.cached(catalog.watchProviders(id, Params.region(region, defaultRegion)));
    }
}
