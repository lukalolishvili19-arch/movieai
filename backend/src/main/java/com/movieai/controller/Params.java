package com.movieai.controller;

import java.time.Duration;
import java.util.Locale;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.movieai.exception.InvalidParameterException;

import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;

/** Shared request-parameter parsing and response helpers for controllers. */
final class Params {

    static final String REGION_PATTERN = "^[A-Za-z]{2}$";
    private static final Pattern PERIOD = Pattern.compile("^(\\d{4})(?:-(\\d{4}))?$");

    /** Browsers and CDNs may briefly cache public catalog responses. */
    static final Duration PUBLIC_MAX_AGE = Duration.ofMinutes(5);

    private Params() {
    }

    static <T> T parse(String field, String raw, Function<String, T> parser) {
        try {
            return parser.apply(raw);
        } catch (IllegalArgumentException e) {
            throw new InvalidParameterException(field, e.getMessage());
        }
    }

    static String region(String raw, String fallback) {
        return raw == null || raw.isBlank() ? fallback.toUpperCase(Locale.ROOT) : raw.toUpperCase(Locale.ROOT);
    }

    static String query(String raw) {
        String trimmed = raw == null ? "" : raw.trim();
        if (trimmed.isEmpty()) {
            throw new InvalidParameterException("query", "must not be blank");
        }
        return trimmed;
    }

    /** "any", "2024" or "2010-2026" → {from, to}; nulls when unconstrained. */
    static Integer[] releasePeriod(String raw) {
        if (raw == null || raw.isBlank() || raw.equalsIgnoreCase("any")) {
            return new Integer[]{null, null};
        }
        Matcher m = PERIOD.matcher(raw.trim().replace('–', '-'));
        if (!m.matches()) {
            throw new InvalidParameterException("releasePeriod", "must be 'any', a year (2024) or a range (2010-2026)");
        }
        int from = Integer.parseInt(m.group(1));
        int to = m.group(2) == null ? from : Integer.parseInt(m.group(2));
        if (from < 1874 || to > 2100 || from > to) {
            throw new InvalidParameterException("releasePeriod", "has an invalid year range");
        }
        return new Integer[]{from, to};
    }

    static void yearRange(Integer from, Integer to) {
        if (from != null && to != null && from > to) {
            throw new InvalidParameterException("yearFrom", "must not be after yearTo");
        }
    }

    static <T> ResponseEntity<T> cached(T body) {
        return ResponseEntity.ok().cacheControl(CacheControl.maxAge(PUBLIC_MAX_AGE).cachePublic()).body(body);
    }
}
