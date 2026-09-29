package com.movieai.config;

/**
 * Public, user-independent caches. User-specific data (watchlist, profile) is never cached here.
 */
public final class CacheNames {

    public static final String TRENDING = "trending";
    public static final String LISTS = "lists";
    public static final String DISCOVER = "discover";
    public static final String SEARCH = "search";
    public static final String DETAILS = "details";
    public static final String REFERENCE = "reference";
    /** Long-lived copies of every public response, served only when TMDB is unavailable. */
    public static final String STALE = "stale";

    private CacheNames() {
    }
}
