package com.movieai.dto.watchlist;

import java.time.Instant;
import java.util.UUID;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import com.movieai.dto.MediaSummary;
import com.movieai.dto.MediaType;
import io.swagger.v3.oas.annotations.media.Schema;

public final class WatchlistDtos {

    private WatchlistDtos() {
    }

    public record AddRequest(
            @NotNull MediaType mediaType,
            @Schema(example = "693134") @NotNull @Positive @Max(Integer.MAX_VALUE) Long tmdbId) {
    }

    public record WatchlistItem(
            UUID id,
            MediaType mediaType,
            long tmdbId,
            Instant addedAt,
            @Schema(nullable = true, description = "Current card data from TMDB; null if temporarily unavailable")
            MediaSummary media) {
    }

    public record WatchlistKey(MediaType mediaType, long tmdbId) {
    }

    public record WatchlistStatus(MediaType mediaType, long tmdbId, boolean inWatchlist) {
    }
}
