package com.movieai.config;

import java.time.Duration;
import java.util.List;

import com.github.benmanes.caffeine.cache.Caffeine;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Caffeine-backed caches with per-category TTLs. Application code only depends on Spring's
 * {@link CacheManager} abstraction, so a Redis-backed manager can replace this bean without
 * touching services or the API contract.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager(MovieAiProperties properties) {
        MovieAiProperties.Cache c = properties.cache();
        SimpleCacheManager manager = new SimpleCacheManager();
        manager.setCaches(List.of(
                build(CacheNames.TRENDING, c.trendingTtl(), c.maxEntries()),
                build(CacheNames.LISTS, c.listsTtl(), c.maxEntries()),
                build(CacheNames.DISCOVER, c.discoverTtl(), c.maxEntries()),
                build(CacheNames.SEARCH, c.searchTtl(), c.maxEntries()),
                build(CacheNames.DETAILS, c.detailsTtl(), c.maxEntries()),
                build(CacheNames.REFERENCE, c.referenceTtl(), 200),
                build(CacheNames.STALE, c.staleTtl(), c.staleMaxEntries())));
        return manager;
    }

    private static CaffeineCache build(String name, Duration ttl, long maxEntries) {
        return new CaffeineCache(name, Caffeine.newBuilder()
                .expireAfterWrite(ttl)
                .maximumSize(maxEntries)
                .recordStats()
                .build());
    }
}
