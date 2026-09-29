package com.movieai.config;

import java.time.Duration;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "movieai")
public record MovieAiProperties(
        @Valid @NotNull Tmdb tmdb,
        @Valid @NotNull Security security,
        @Valid @NotNull RateLimit rateLimit,
        @Valid @NotNull Cache cache) {

    public record Tmdb(
            String accessToken,
            @NotBlank String apiBaseUrl,
            @NotBlank String defaultImageBaseUrl,
            @NotBlank String language,
            @NotBlank String defaultRegion,
            @NotNull Duration connectTimeout,
            @NotNull Duration readTimeout,
            @Min(1) int maxConcurrentRequests) {
    }

    public record Security(
            @Valid @NotNull Jwt jwt,
            @Valid @NotNull RefreshCookie refreshCookie,
            @Valid @NotNull Cors cors) {
    }

    public record Jwt(
            String secret,
            String refreshSecret,
            @NotBlank String issuer,
            @NotNull Duration accessTokenTtl,
            @NotNull Duration refreshTokenTtl) {
    }

    public record RefreshCookie(
            @NotBlank String name,
            @NotBlank String path,
            boolean secure,
            @NotBlank String sameSite) {
    }

    public record Cors(List<String> allowedOrigins) {
    }

    public record RateLimit(
            boolean enabled,
            @Min(1) int publicPerMinute,
            @Min(1) int searchPerMinute,
            @Min(1) int recommendationsPerMinute,
            @Min(1) int authPerMinute,
            @Min(1) int watchlistPerMinute) {
    }

    public record Cache(
            @NotNull Duration trendingTtl,
            @NotNull Duration listsTtl,
            @NotNull Duration discoverTtl,
            @NotNull Duration searchTtl,
            @NotNull Duration detailsTtl,
            @NotNull Duration referenceTtl,
            @NotNull Duration staleTtl,
            @Min(100) long maxEntries,
            @Min(100) long staleMaxEntries) {
    }
}
