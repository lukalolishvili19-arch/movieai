package com.movieai.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Where-to-watch availability for one region. Data provided by JustWatch via TMDB.")
public record WatchProviders(
        @Schema(example = "US") String region,
        @Schema(nullable = true, description = "TMDB watch page for this title and region") String link,
        List<WatchProvider> flatrate,
        List<WatchProvider> free,
        List<WatchProvider> ads,
        List<WatchProvider> rent,
        List<WatchProvider> buy,
        @Schema(example = "JustWatch") String attribution) {

    public static WatchProviders none(String region) {
        return new WatchProviders(region, null, List.of(), List.of(), List.of(), List.of(), List.of(), "JustWatch");
    }
}
