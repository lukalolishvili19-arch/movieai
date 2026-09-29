package com.movieai.security;

import java.time.Duration;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

/**
 * In-memory token-bucket limiter keyed by client and bucket name. Each bucket holds
 * {@code perMinute} tokens and refills continuously. Idle buckets are evicted.
 * For multi-instance deployments this can be backed by Redis without changing callers.
 */
public class RateLimiter {

    public record Decision(boolean allowed, int limit, int remaining, long retryAfterSeconds) {
    }

    private final Cache<String, Bucket> buckets = Caffeine.newBuilder()
            .expireAfterAccess(Duration.ofMinutes(10))
            .maximumSize(100_000)
            .build();

    public Decision tryConsume(String key, int perMinute) {
        Bucket bucket = buckets.get(key, k -> new Bucket(perMinute));
        return bucket.tryConsume(perMinute);
    }

    private static final class Bucket {
        private double tokens;
        private long lastRefillNanos;

        Bucket(int capacity) {
            this.tokens = capacity;
            this.lastRefillNanos = System.nanoTime();
        }

        synchronized Decision tryConsume(int capacity) {
            long now = System.nanoTime();
            double refillPerNano = capacity / 60_000_000_000d;
            tokens = Math.min(capacity, tokens + (now - lastRefillNanos) * refillPerNano);
            lastRefillNanos = now;
            if (tokens >= 1) {
                tokens -= 1;
                return new Decision(true, capacity, (int) Math.floor(tokens), 0);
            }
            long retryAfter = (long) Math.ceil((1 - tokens) / refillPerNano / 1_000_000_000d);
            return new Decision(false, capacity, 0, Math.max(1, retryAfter));
        }
    }
}
