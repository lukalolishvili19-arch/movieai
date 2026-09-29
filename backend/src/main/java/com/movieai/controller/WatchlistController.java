package com.movieai.controller;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;

import com.movieai.dto.MediaType;
import com.movieai.dto.PagedResponse;
import com.movieai.dto.watchlist.WatchlistDtos.AddRequest;
import com.movieai.dto.watchlist.WatchlistDtos.WatchlistItem;
import com.movieai.dto.watchlist.WatchlistDtos.WatchlistKey;
import com.movieai.dto.watchlist.WatchlistDtos.WatchlistStatus;
import com.movieai.security.AuthenticatedUser;
import com.movieai.service.WatchlistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/watchlist")
@Tag(name = "Watchlist", description = "The signed-in user's persistent watchlist. Requires a Bearer access token.")
@SecurityRequirement(name = "bearerAuth")
public class WatchlistController {

    private final WatchlistService watchlist;

    public WatchlistController(WatchlistService watchlist) {
        this.watchlist = watchlist;
    }

    @GetMapping
    @Operation(summary = "List watchlist items", description = "Newest first, with current card data for each title.")
    public ResponseEntity<PagedResponse<WatchlistItem>> list(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Parameter(description = "Filter by media type", schema = @Schema(allowableValues = {"movie", "tv"}))
            @RequestParam(required = false) String mediaType,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size) {
        MediaType type = mediaType == null || mediaType.isBlank() ? null
                : Params.parse("mediaType", mediaType, MediaType::fromValue);
        return privateBody(watchlist.list(user.id(), type, page, size));
    }

    @GetMapping("/ids")
    @Operation(summary = "All watchlist keys",
            description = "Lightweight list of (mediaType, tmdbId) pairs used to render +/✓ states on cards.")
    public ResponseEntity<List<WatchlistKey>> ids(@AuthenticationPrincipal AuthenticatedUser user) {
        return privateBody(watchlist.keys(user.id()));
    }

    @GetMapping("/check/{mediaType}/{tmdbId}")
    @Operation(summary = "Check whether a title is in the watchlist")
    public ResponseEntity<WatchlistStatus> check(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Parameter(schema = @Schema(allowableValues = {"movie", "tv"})) @PathVariable String mediaType,
            @PathVariable @Positive long tmdbId) {
        MediaType type = Params.parse("mediaType", mediaType, MediaType::fromValue);
        return privateBody(new WatchlistStatus(type, tmdbId, watchlist.contains(user.id(), type, tmdbId)));
    }

    @PostMapping
    @Operation(summary = "Add a title to the watchlist",
            description = "Idempotent: 201 when added, 200 when it was already present. The title must exist on TMDB.")
    public ResponseEntity<WatchlistItem> add(@AuthenticationPrincipal AuthenticatedUser user,
                                             @Valid @RequestBody AddRequest request) {
        WatchlistService.AddResult result = watchlist.add(user.id(), request.mediaType(), request.tmdbId());
        return ResponseEntity.status(result.created() ? HttpStatus.CREATED : HttpStatus.OK)
                .cacheControl(CacheControl.noStore())
                .body(result.item());
    }

    @DeleteMapping("/{mediaType}/{tmdbId}")
    @Operation(summary = "Remove a title from the watchlist", description = "Idempotent; always 204.")
    public ResponseEntity<Void> remove(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Parameter(schema = @Schema(allowableValues = {"movie", "tv"})) @PathVariable String mediaType,
            @PathVariable @Positive long tmdbId) {
        watchlist.remove(user.id(), Params.parse("mediaType", mediaType, MediaType::fromValue), tmdbId);
        return ResponseEntity.noContent().build();
    }

    private static <T> ResponseEntity<T> privateBody(T body) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore().cachePrivate()).body(body);
    }
}
