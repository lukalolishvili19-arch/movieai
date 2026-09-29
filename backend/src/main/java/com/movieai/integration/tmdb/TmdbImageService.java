package com.movieai.integration.tmdb;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import com.movieai.config.MovieAiProperties;
import com.movieai.integration.tmdb.model.TmdbModels;
import com.movieai.service.image.ImageUrlResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;

/**
 * Builds TMDB image URLs from file paths using TMDB's /configuration (secure base URL and the
 * sizes it actually offers). Purpose-based sizes avoid ever requesting "original" images.
 */
@Service
public class TmdbImageService implements ImageUrlResolver {

    private static final Logger log = LoggerFactory.getLogger(TmdbImageService.class);
    private static final Duration REFRESH_INTERVAL = Duration.ofHours(24);
    private static final Duration RETRY_INTERVAL = Duration.ofMinutes(5);

    private static final Map<Kind, Map<Size, String>> PREFERRED = Map.of(
            Kind.POSTER, Map.of(Size.THUMB, "w154", Size.CARD, "w342", Size.DETAIL, "w500", Size.HERO, "w780"),
            Kind.BACKDROP, Map.of(Size.THUMB, "w300", Size.CARD, "w780", Size.DETAIL, "w1280", Size.HERO, "w1280"),
            Kind.PROFILE, Map.of(Size.THUMB, "w45", Size.CARD, "w185", Size.DETAIL, "h632", Size.HERO, "h632"),
            Kind.LOGO, Map.of(Size.THUMB, "w45", Size.CARD, "w92", Size.DETAIL, "w154", Size.HERO, "w300"),
            Kind.STILL, Map.of(Size.THUMB, "w92", Size.CARD, "w300", Size.DETAIL, "w300", Size.HERO, "w300"));

    private final TmdbClient client;
    private final AtomicReference<Snapshot> snapshot;
    private volatile Instant lastAttempt = Instant.EPOCH;

    public TmdbImageService(TmdbClient client, MovieAiProperties properties) {
        this.client = client;
        this.snapshot = new AtomicReference<>(Snapshot.defaults(properties.tmdb().defaultImageBaseUrl()));
    }

    @Override
    public String url(String path, Kind kind, Size size) {
        if (path == null || path.isBlank()) {
            return null;
        }
        Snapshot current = current();
        String sizeToken = choose(current.sizes(kind), PREFERRED.get(kind).get(size));
        String normalizedPath = path.startsWith("/") ? path : "/" + path;
        return current.baseUrl() + sizeToken + normalizedPath;
    }

    private Snapshot current() {
        Snapshot current = snapshot.get();
        Instant now = Instant.now();
        boolean stale = current.fetchedAt() == null || current.fetchedAt().plus(REFRESH_INTERVAL).isBefore(now);
        if (stale && lastAttempt.plus(RETRY_INTERVAL).isBefore(now)) {
            refresh(now);
        }
        return snapshot.get();
    }

    private synchronized void refresh(Instant now) {
        if (!lastAttempt.plus(RETRY_INTERVAL).isBefore(now)) {
            return;
        }
        lastAttempt = now;
        try {
            TmdbModels.Configuration config = client.get("/configuration", Map.of(), TmdbModels.Configuration.class);
            if (config.images() != null && config.images().secureBaseUrl() != null) {
                snapshot.set(Snapshot.from(config.images(), now, snapshot.get()));
            }
        } catch (RuntimeException e) {
            log.info("Using default TMDB image configuration: {}", e.getMessage());
        }
    }

    /**
     * Picks the preferred size when offered; otherwise the smallest offered width that is at least
     * as large; otherwise the largest non-original size.
     */
    static String choose(List<String> available, String preferred) {
        if (available == null || available.isEmpty() || available.contains(preferred)) {
            return preferred;
        }
        int target = width(preferred);
        String best = null;
        int bestWidth = Integer.MAX_VALUE;
        String largest = null;
        int largestWidth = -1;
        for (String candidate : available) {
            if (!candidate.startsWith("w")) {
                continue;
            }
            int w = width(candidate);
            if (w >= target && w < bestWidth) {
                best = candidate;
                bestWidth = w;
            }
            if (w > largestWidth) {
                largest = candidate;
                largestWidth = w;
            }
        }
        if (best != null) {
            return best;
        }
        return largest != null ? largest : preferred;
    }

    private static int width(String token) {
        try {
            int px = Integer.parseInt(token.substring(1));
            return token.startsWith("h") ? px * 2 / 3 : px;
        } catch (RuntimeException e) {
            return Integer.MAX_VALUE;
        }
    }

    private record Snapshot(String baseUrl, Map<Kind, List<String>> sizesByKind, Instant fetchedAt) {

        static Snapshot defaults(String baseUrl) {
            return new Snapshot(withSlash(baseUrl), Map.of(
                    Kind.POSTER, List.of("w92", "w154", "w185", "w342", "w500", "w780", "original"),
                    Kind.BACKDROP, List.of("w300", "w780", "w1280", "original"),
                    Kind.PROFILE, List.of("w45", "w185", "h632", "original"),
                    Kind.LOGO, List.of("w45", "w92", "w154", "w185", "w300", "w500", "original"),
                    Kind.STILL, List.of("w92", "w185", "w300", "original")), null);
        }

        static Snapshot from(TmdbModels.ImageConfiguration images, Instant now, Snapshot fallback) {
            return new Snapshot(withSlash(images.secureBaseUrl()), Map.of(
                    Kind.POSTER, orElse(images.posterSizes(), fallback.sizes(Kind.POSTER)),
                    Kind.BACKDROP, orElse(images.backdropSizes(), fallback.sizes(Kind.BACKDROP)),
                    Kind.PROFILE, orElse(images.profileSizes(), fallback.sizes(Kind.PROFILE)),
                    Kind.LOGO, orElse(images.logoSizes(), fallback.sizes(Kind.LOGO)),
                    Kind.STILL, orElse(images.stillSizes(), fallback.sizes(Kind.STILL))), now);
        }

        List<String> sizes(Kind kind) {
            return sizesByKind.get(kind);
        }

        private static List<String> orElse(List<String> value, List<String> fallback) {
            return value == null || value.isEmpty() ? fallback : value;
        }

        private static String withSlash(String url) {
            return url.endsWith("/") ? url : url + "/";
        }
    }
}
