package com.movieai.integration.tmdb;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import com.movieai.config.MovieAiProperties;
import com.movieai.exception.ExternalServiceUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Low-level TMDB HTTP client. Handles authentication, default language, concurrency limiting,
 * a single retry for transient failures, and translation of HTTP failures into application
 * exceptions so no TMDB error detail reaches API clients.
 */
@Component
public class TmdbClient {

    private static final Logger log = LoggerFactory.getLogger(TmdbClient.class);
    private static final long RETRY_BACKOFF_MS = 400;
    private static final long MAX_RETRY_AFTER_MS = 2_000;

    private final RestClient restClient;
    private final MovieAiProperties.Tmdb properties;
    private final Semaphore permits;

    public TmdbClient(@Qualifier("tmdbRestClient") RestClient restClient, MovieAiProperties properties) {
        this.restClient = restClient;
        this.properties = properties.tmdb();
        this.permits = new Semaphore(this.properties.maxConcurrentRequests());
    }

    public <T> T get(String path, Map<String, ?> params, Class<T> type) {
        return execute(path, params, uri -> restClient.get().uri(uri).retrieve().body(type));
    }

    public <T> T get(String path, Map<String, ?> params, ParameterizedTypeReference<T> type) {
        return execute(path, params, uri -> restClient.get().uri(uri).retrieve().body(type));
    }

    private <T> T execute(String path, Map<String, ?> params, Call<T> call) {
        if (properties.accessToken() == null || properties.accessToken().isBlank()) {
            throw new ExternalServiceUnavailableException("TMDB_ACCESS_TOKEN is not configured", null);
        }
        URI uri = buildUri(path, params);
        acquire(path);
        try {
            return callWithRetry(path, uri, call);
        } finally {
            permits.release();
        }
    }

    private <T> T callWithRetry(String path, URI uri, Call<T> call) {
        for (int attempt = 1; ; attempt++) {
            try {
                T body = call.run(uri);
                if (body == null) {
                    throw new ExternalServiceUnavailableException("Empty TMDB response for " + path, null);
                }
                return body;
            } catch (RestClientResponseException e) {
                HttpStatusCode status = e.getStatusCode();
                if (status.value() == 404) {
                    throw new TmdbNotFoundException(path);
                }
                if (status.value() == 401 || status.value() == 403) {
                    log.error("TMDB rejected credentials (HTTP {}). Check TMDB_ACCESS_TOKEN.", status.value());
                    throw new ExternalServiceUnavailableException("TMDB authentication failed", e);
                }
                boolean transientError = status.value() == 429 || status.is5xxServerError();
                if (transientError && attempt == 1) {
                    sleep(retryDelay(e));
                    continue;
                }
                throw new ExternalServiceUnavailableException("TMDB HTTP " + status.value() + " for " + path, e);
            } catch (ResourceAccessException e) {
                if (attempt == 1) {
                    sleep(RETRY_BACKOFF_MS);
                    continue;
                }
                throw new ExternalServiceUnavailableException("TMDB unreachable for " + path, e);
            } catch (RestClientException e) {
                throw new ExternalServiceUnavailableException("TMDB call failed for " + path, e);
            }
        }
    }

    private URI buildUri(String path, Map<String, ?> params) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(properties.apiBaseUrl()).path(path);
        if (params == null || !params.containsKey("language")) {
            builder.queryParam("language", properties.language());
        }
        if (params != null) {
            params.forEach((key, value) -> {
                if (value != null && !String.valueOf(value).isBlank()) {
                    builder.queryParam(key, value);
                }
            });
        }
        return builder.encode().build().toUri();
    }

    private void acquire(String path) {
        try {
            if (!permits.tryAcquire(5, TimeUnit.SECONDS)) {
                throw new ExternalServiceUnavailableException("TMDB concurrency limit reached for " + path, null);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ExternalServiceUnavailableException("Interrupted waiting for TMDB permit", e);
        }
    }

    private static long retryDelay(RestClientResponseException e) {
        String retryAfter = e.getResponseHeaders() == null ? null : e.getResponseHeaders().getFirst("Retry-After");
        if (retryAfter != null) {
            try {
                return Math.min(MAX_RETRY_AFTER_MS, Long.parseLong(retryAfter.trim()) * 1000);
            } catch (NumberFormatException ignored) {
                // fall through to the default backoff
            }
        }
        return RETRY_BACKOFF_MS;
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @FunctionalInterface
    private interface Call<T> {
        T run(URI uri);
    }
}
