package com.movieai.integration.tmdb;

/** TMDB answered 404. Services translate this into a domain-specific not-found error. */
public class TmdbNotFoundException extends RuntimeException {

    public TmdbNotFoundException(String path) {
        super("TMDB resource not found: " + path);
    }
}
