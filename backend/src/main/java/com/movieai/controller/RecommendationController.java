package com.movieai.controller;

import java.util.List;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import com.movieai.dto.RecommendationResponse;
import com.movieai.service.RecommendationService;
import com.movieai.service.browse.RuntimeRange;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/recommendations")
@Tag(name = "Recommendations", description = "\"What Should I Watch?\" picks. Public; rate limited per client.")
public class RecommendationController {

    private final RecommendationService recommendations;

    public RecommendationController(RecommendationService recommendations) {
        this.recommendations = recommendations;
    }

    @GetMapping("/movies")
    @Operation(summary = "Random movie picks matching filters",
            description = "Returns `limit` movies (default 10) matching the filters. The first call returns a seed; "
                    + "pass the same seed with page=2, 3, ... for \"Give Me 10 More\" — each page is a new, "
                    + "non-overlapping batch until hasMore is false.")
    public ResponseEntity<RecommendationResponse> movies(
            @Parameter(description = "Genre ids (comma-separated); a movie matching any of them qualifies")
            @RequestParam(required = false) @Size(max = 10) List<@Positive Integer> genres,
            @Parameter(description = "Minimum TMDB rating (0-10)", example = "7")
            @RequestParam(required = false) @DecimalMin("0") @DecimalMax("10") Double minRating,
            @Parameter(description = "'any', a single year (2026) or a range (2020-2026)", example = "2020-2026")
            @RequestParam(defaultValue = "any") String releasePeriod,
            @Parameter(schema = @Schema(allowableValues = {"any", "under-90", "90-120", "over-120"}))
            @RequestParam(defaultValue = "any") String runtime,
            @RequestParam(defaultValue = "1") @Min(1) @Max(1000) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(20) int limit,
            @Parameter(description = "Seed returned by a previous call") @RequestParam(required = false) @PositiveOrZero Long seed) {
        Integer[] years = Params.releasePeriod(releasePeriod);
        RecommendationResponse response = recommendations.movies(genres, minRating, years[0], years[1],
                Params.parse("runtime", runtime, RuntimeRange::fromValue), page, limit, seed);
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(response);
    }
}
