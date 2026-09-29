package com.movieai.integration.tmdb;

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TmdbImageServiceTest {

    private static final List<String> POSTERS = List.of("w92", "w154", "w185", "w342", "w500", "w780", "original");

    @Test
    void usesPreferredSizeWhenOffered() {
        assertThat(TmdbImageService.choose(POSTERS, "w342")).isEqualTo("w342");
    }

    @Test
    void fallsBackToNextLargerWidthNeverOriginal() {
        assertThat(TmdbImageService.choose(List.of("w92", "w500", "original"), "w342")).isEqualTo("w500");
        assertThat(TmdbImageService.choose(List.of("w92", "w185", "original"), "w342")).isEqualTo("w185");
    }
}
