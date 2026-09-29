package com.movieai.controller;

import java.util.List;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;

import com.movieai.dto.ImageAsset;
import com.movieai.dto.PagedResponse;
import com.movieai.dto.PersonCredits;
import com.movieai.dto.PersonDetails;
import com.movieai.dto.PersonSummary;
import com.movieai.service.catalog.PersonCatalog;
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
@RequestMapping("/api/v1/people")
@Tag(name = "People", description = "Actors and crew. No authentication required.")
public class PersonController {

    private final PersonCatalog catalog;

    public PersonController(PersonCatalog catalog) {
        this.catalog = catalog;
    }

    @GetMapping
    @Operation(summary = "Popular people")
    public ResponseEntity<PagedResponse<PersonSummary>> popular(@RequestParam(defaultValue = "1") @Min(1) @Max(500) int page) {
        return Params.cached(catalog.popular(page));
    }

    @GetMapping("/trending")
    @Operation(summary = "Trending people")
    public ResponseEntity<PagedResponse<PersonSummary>> trending(
            @Parameter(schema = @Schema(allowableValues = {"day", "week"})) @RequestParam(defaultValue = "day") String period,
            @RequestParam(defaultValue = "1") @Min(1) @Max(500) int page) {
        return Params.cached(catalog.trending(Params.parse("period", period, TimeWindow::fromValue), page));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Person details",
            description = "Biography and facts exactly as provided by TMDB, plus filmography and photos. "
                    + "Returns PERSON_NOT_FOUND (404) for unknown ids.")
    public ResponseEntity<PersonDetails> details(@PathVariable @Positive long id) {
        return Params.cached(catalog.details(id));
    }

    @GetMapping("/{id}/credits")
    @Operation(summary = "Person filmography", description = "Movie and TV credits (cast and crew combined per title).")
    public ResponseEntity<PersonCredits> credits(@PathVariable @Positive long id) {
        return Params.cached(catalog.credits(id));
    }

    @GetMapping("/{id}/images")
    @Operation(summary = "Person photos")
    public ResponseEntity<List<ImageAsset>> images(@PathVariable @Positive long id) {
        return Params.cached(catalog.images(id));
    }
}
