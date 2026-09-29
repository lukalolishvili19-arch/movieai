package com.movieai.service.browse;

import java.time.LocalDate;
import java.util.List;

import com.movieai.service.catalog.DiscoverQuery.SortOption;

/** User-facing browse filters, shared by movies and TV. */
public record BrowseFilters(
        List<Integer> genres,
        Double minRating,
        Integer yearFrom,
        Integer yearTo,
        RuntimeRange runtime,
        SortOption sort) {

    /** Minimum vote count applied with a rating filter so obscure titles with a handful of votes don't dominate. */
    static final int MIN_VOTES_WITH_RATING = 50;

    public boolean isEmpty() {
        return (genres == null || genres.isEmpty()) && minRating == null && yearFrom == null && yearTo == null
                && (runtime == null || runtime == RuntimeRange.ANY) && sort == null;
    }

    LocalDate from() {
        return yearFrom == null ? null : LocalDate.of(yearFrom, 1, 1);
    }

    LocalDate to() {
        return yearTo == null ? null : LocalDate.of(yearTo, 12, 31);
    }

    static LocalDate later(LocalDate a, LocalDate b) {
        if (a == null) {
            return b;
        }
        if (b == null) {
            return a;
        }
        return a.isAfter(b) ? a : b;
    }

    static LocalDate earlier(LocalDate a, LocalDate b) {
        if (a == null) {
            return b;
        }
        if (b == null) {
            return a;
        }
        return a.isBefore(b) ? a : b;
    }
}
