package com.movieai.security;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.movieai.config.MovieAiProperties;
import com.movieai.exception.ErrorCode;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Per-client rate limiting with separate budgets for authentication, search, recommendations,
 * watchlist and general public API traffic. Authenticated watchlist calls are keyed by user,
 * everything else by client IP.
 */
public class RateLimitFilter extends OncePerRequestFilter {

    private static final String API_PREFIX = "/api/";

    private final RateLimiter limiter;
    private final MovieAiProperties.RateLimit properties;
    private final JsonErrorWriter errorWriter;

    public RateLimitFilter(RateLimiter limiter, MovieAiProperties.RateLimit properties, JsonErrorWriter errorWriter) {
        this.limiter = limiter;
        this.properties = properties;
        this.errorWriter = errorWriter;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !properties.enabled() || "OPTIONS".equalsIgnoreCase(request.getMethod())
                || !request.getRequestURI().startsWith(API_PREFIX);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String path = request.getRequestURI();
        String bucket;
        int limit;
        if (path.startsWith("/api/v1/auth/login") || path.startsWith("/api/v1/auth/register")) {
            bucket = "auth";
            limit = properties.authPerMinute();
        } else if (path.startsWith("/api/v1/auth/")) {
            bucket = "session";
            limit = properties.watchlistPerMinute();
        } else if (path.startsWith("/api/v1/search")) {
            bucket = "search";
            limit = properties.searchPerMinute();
        } else if (path.startsWith("/api/v1/recommendations")) {
            bucket = "recommendations";
            limit = properties.recommendationsPerMinute();
        } else if (path.startsWith("/api/v1/watchlist")) {
            bucket = "watchlist";
            limit = properties.watchlistPerMinute();
        } else {
            bucket = "public";
            limit = properties.publicPerMinute();
        }

        RateLimiter.Decision decision = limiter.tryConsume(bucket + ":" + clientKey(request, bucket), limit);
        response.setHeader("X-RateLimit-Limit", String.valueOf(decision.limit()));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(decision.remaining()));
        if (!decision.allowed()) {
            response.setHeader("Retry-After", String.valueOf(decision.retryAfterSeconds()));
            errorWriter.write(response, ErrorCode.RATE_LIMITED);
            return;
        }
        chain.doFilter(request, response);
    }

    private static String clientKey(HttpServletRequest request, String bucket) {
        if ("watchlist".equals(bucket)) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof AuthenticatedUser user) {
                return "user:" + user.id();
            }
        }
        return "ip:" + request.getRemoteAddr();
    }
}
