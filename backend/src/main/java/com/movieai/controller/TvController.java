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
import com.movieai.dto.PagedResponse;
import com.movieai.dto.TvDetails;
import com.movieai.dto.TvSummary;
import com.movieai.dto.VideoAsset;
import com.movieai.dto.WatchProviders;
import com.movieai.service.browse.BrowseFilters;
import com.movieai.service.browse.BrowseService;
import com.movieai.service.browse.BrowseService.TvCategory;
import com.movieai.service.browse.RuntimeRange;
import com.movieai.service.catalog.DiscoverQuery.SortOption;
import com.movieai.service.catalog.TimeWindow;
import com.movieai.service.catalog.TvCatalog;
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
@RequestMapping("/api/v1/tv")
@Tag(name = "TV", description = "Public TV lists, discovery and details. No authentication required.")
public class TvController {

    private final TvCatalog catalog;
    private final BrowseService browse;
    private final String defaultRegion;

    public TvController(TvCatalog catalog, BrowseService browse, MovieAiProperties properties) {
        this.catalog = catalog;
        this.browse = browse;
        this.defaultRegion = properties.tmdb().defaultRegion();
    }

    @GetMapping
    @Operation(summary = "Browse TV shows",
            description = "Paginated TV browsing with server-side filtering. runtime refers to episode runtime.")
    public ResponseEntity<PagedResponse<TvSummary>> browse(
            @Parameter(schema = @Schema(allowableValues = {"trending", "popular", "top-rated", "airing-today", "on-the-air", "upcoming"}))
            @RequestParam(defaultValue = "popular") String category,
            @Parameter(schema = @Schema(allowableValues = {"day", "week"})) @RequestParam(defaultValue = "day") String period,
            @RequestParam(required = false) @Size(max = 10) List<@Positive Integer> genres,
            @RequestParam(required = false) @DecimalMin("0") @DecimalMax("10") Double minRating,
            @Parameter(description = "Earliest first-air year") @RequestParam(required = false) @Min(1900) @Max(2100) Integer yearFrom,
            @Parameter(description = "Latest first-air year") @RequestParam(required = false) @Min(1900) @Max(2100) Integer yearTo,
            @Parameter(schema = @Schema(allowableValues = {"any", "under-30", "30-60", "over-60"}))
            @RequestParam(required = false) String runtime,
            @Parameter(schema = @Schema(allowableValues = {"popularity", "rating", "release_date", "title"}))
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "1") @Min(1) @Max(500) int page) {
        Params.yearRange(yearFrom, yearTo);
        TvCategory cat = Params.parse("category", category,
                v -> TvCategory.valueOf(v.trim().toUpperCase().replace('-', '_')));
        BrowseFilters filters = new BrowseFilters(genres, minRating, yearFrom, yearTo,
                Params.parse("runtime", runtime, RuntimeRange::fromValue),
                Params.parse("sort", sort, SortOption::fromValue));
        return Params.cached(browse.tv(cat, Params.parse("period", period, TimeWindow::fromValue), filters, page));
    }

    @GetMapping("/trending")
    @Operation(summary = "Trending TV shows")
    public ResponseEntity<PagedResponse<TvSummary>> trending(
            @Parameter(schema = @Schema(allowableValues = {"day", "week"})) @RequestParam(defaultValue = "day") String period,
            @RequestParam(defaultValue = "1") @Min(1) @Max(500) int page) {
        return Params.cached(catalog.trending(Params.parse("period", period, TimeWindow::fromValue), page));
    }

    @GetMapping("/popular")
    @Operation(summary = "Popular TV shows")
    public ResponseEntity<PagedResponse<TvSummary>> popular(@RequestParam(defaultValue = "1") @Min(1) @Max(500) int page) {
        return Params.cached(catalog.popular(page));
    }

    @GetMapping("/airing-today")
    @Operation(summary = "TV shows with an episode airing today")
    public ResponseEntity<PagedResponse<TvSummary>> airingToday(@RequestParam(defaultValue = "1") @Min(1) @Max(500) int page) {
        return Params.cached(catalog.airingToday(page));
    }

    @GetMapping("/on-the-air")
    @Operation(summary = "TV shows airing in the next 7 days")
    public ResponseEntity<PagedResponse<TvSummary>> onTheAir(@RequestParam(defaultValue = "1") @Min(1) @Max(500) int page) {
        return Params.cached(catalog.onTheAir(page));
    }

    @GetMapping("/top-rated")
    @Operation(summary = "Top rated TV shows")
    public ResponseEntity<PagedResponse<TvSummary>> topRated(@RequestParam(defaultValue = "1") @Min(1) @Max(500) int page) {
        return Params.cached(catalog.topRated(page));
    }

    @GetMapping("/genres")
    @Operation(summary = "TV genres")
    public ResponseEntity<List<Genre>> genres() {
        return Params.cached(catalog.genres());
    }

    @GetMapping("/{id}")
    @Operation(summary = "TV show details",
            description = "Details with real season data, cast, images, videos, trailer, similar shows and watch "
                    + "providers. Returns TV_NOT_FOUND (404) for unknown ids.")
    public ResponseEntity<TvDetails> details(
            @PathVariable @Positive long id,
            @Parameter(description = "ISO 3166-1 country code for watch providers", example = "US")
            @RequestParam(required = false) @Pattern(regexp = Params.REGION_PATTERN) String region) {
        return Params.cached(catalog.details(id, Params.region(region, defaultRegion)));
    }

    @GetMapping("/{id}/images")
    @Operation(summary = "TV show images")
    public ResponseEntity<MediaImages> images(@PathVariable @Positive long id) {
        return Params.cached(catalog.images(id));
    }

    @GetMapping("/{id}/videos")
    @Operation(summary = "TV show videos")
    public ResponseEntity<List<VideoAsset>> videos(@PathVariable @Positive long id) {
        return Params.cached(catalog.videos(id));
    }

    @GetMapping("/{id}/watch-providers")
    @Operation(summary = "Where to watch a TV show", description = "Data by JustWatch.")
    public ResponseEntity<WatchProviders> watchProviders(
            @PathVariable @Positive long id,
            @RequestParam(required = false) @Pattern(regexp = Params.REGION_PATTERN) String region) {
        return Params.cached(catalog.watchProviders(id, Params.region(region, defaultRegion)));
    }
}
