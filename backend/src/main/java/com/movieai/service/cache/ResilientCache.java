package com.movieai.service.cache;

import java.util.Objects;
import java.util.function.Supplier;

import com.movieai.config.CacheNames;
import com.movieai.exception.ExternalServiceUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

/**
 * Read-through cache for public provider data.
 * <ul>
 *   <li>Concurrent requests for the same key share a single upstream call.</li>
 *   <li>Every successful load is also written to a long-lived stale cache.</li>
 *   <li>If the provider is unavailable and the fresh entry has expired, the stale copy is served.</li>
 * </ul>
 */
@Component
public class ResilientCache {

    private static final Logger log = LoggerFactory.getLogger(ResilientCache.class);

    private final CacheManager cacheManager;

    public ResilientCache(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String cacheName, String key, Supplier<T> loader) {
        Cache fresh = Objects.requireNonNull(cacheManager.getCache(cacheName), cacheName);
        Cache stale = Objects.requireNonNull(cacheManager.getCache(CacheNames.STALE));
        String staleKey = cacheName + "::" + key;
        try {
            return fresh.get(key, () -> {
                T loaded = loader.get();
                if (loaded != null) {
                    stale.put(staleKey, loaded);
                }
                return loaded;
            });
        } catch (Cache.ValueRetrievalException e) {
            Throwable cause = e.getCause();
            if (cause instanceof ExternalServiceUnavailableException unavailable) {
                Cache.ValueWrapper wrapper = stale.get(staleKey);
                if (wrapper != null && wrapper.get() != null) {
                    log.warn("Serving stale cache entry {} because the provider is unavailable", staleKey);
                    return (T) wrapper.get();
                }
                throw unavailable;
            }
            if (cause instanceof RuntimeException runtime) {
                throw runtime;
            }
            throw e;
        }
    }
}
