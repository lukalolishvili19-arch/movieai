package com.movieai.security;

import java.time.Duration;
import java.time.Instant;

import com.movieai.config.MovieAiProperties;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

/** The refresh token travels only in an HttpOnly cookie scoped to the auth endpoints. */
@Component
public class RefreshCookieService {

    private final MovieAiProperties.RefreshCookie properties;

    public RefreshCookieService(MovieAiProperties properties) {
        this.properties = properties.security().refreshCookie();
    }

    public String name() {
        return properties.name();
    }

    public ResponseCookie create(String token, Instant expiresAt) {
        return base(token).maxAge(Duration.between(Instant.now(), expiresAt)).build();
    }

    public ResponseCookie clear() {
        return base("").maxAge(Duration.ZERO).build();
    }

    private ResponseCookie.ResponseCookieBuilder base(String value) {
        return ResponseCookie.from(properties.name(), value)
                .httpOnly(true)
                .secure(properties.secure())
                .sameSite(properties.sameSite())
                .path(properties.path());
    }
}
