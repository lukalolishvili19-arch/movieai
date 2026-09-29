package com.movieai.service.catalog;

import java.time.LocalDate;
import java.util.List;

/**
 * Provider-neutral discovery filter. Null fields are "no constraint".
 *
 * @param matchAnyGenre true = results with any of the genres (OR), false = all genres (AND)
 * @param airDateFrom   TV only: an episode airs on/after this date
 * @param airDateTo     TV only: an episode airs on/before this date
 * @param theatrical    movies only: restrict to theatrical releases
 */
public record DiscoverQuery(
        List<Integer> genres,
        boolean matchAnyGenre,
        Double minRating,
        Integer minVotes,
        LocalDate releaseFrom,
        LocalDate releaseTo,
        Integer runtimeMin,
        Integer runtimeMax,
        SortOption sort,
        LocalDate airDateFrom,
        LocalDate airDateTo,
        boolean theatrical,
        int page) {

    public enum SortOption {
        POPULARITY, RATING, RELEASE_DATE, TITLE;

        public static SortOption fromValue(String raw) {
            if (raw == null || raw.isBlank()) {
                return null;
            }
            return switch (raw.trim().toLowerCase()) {
                case "popularity" -> POPULARITY;
                case "rating" -> RATING;
                case "release_date", "newest" -> RELEASE_DATE;
                case "title" -> TITLE;
                default -> throw new IllegalArgumentException("sort must be one of popularity, rating, release_date, title");
            };
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public String cacheKey() {
        return String.join("|",
                String.valueOf(genres == null ? "" : genres.stream().sorted().toList()),
                String.valueOf(matchAnyGenre), String.valueOf(minRating), String.valueOf(minVotes),
                String.valueOf(releaseFrom), String.valueOf(releaseTo), String.valueOf(runtimeMin),
                String.valueOf(runtimeMax), String.valueOf(sort), String.valueOf(airDateFrom),
                String.valueOf(airDateTo), String.valueOf(theatrical), String.valueOf(page));
    }

    public static final class Builder {
        private List<Integer> genres = List.of();
        private boolean matchAnyGenre;
        private Double minRating;
        private Integer minVotes;
        private LocalDate releaseFrom;
        private LocalDate releaseTo;
        private Integer runtimeMin;
        private Integer runtimeMax;
        private SortOption sort;
        private LocalDate airDateFrom;
        private LocalDate airDateTo;
        private boolean theatrical;
        private int page = 1;

        public Builder genres(List<Integer> v) { this.genres = v == null ? List.of() : v; return this; }
        public Builder matchAnyGenre(boolean v) { this.matchAnyGenre = v; return this; }
        public Builder minRating(Double v) { this.minRating = v; return this; }
        public Builder minVotes(Integer v) { this.minVotes = v; return this; }
        public Builder releaseFrom(LocalDate v) { this.releaseFrom = v; return this; }
        public Builder releaseTo(LocalDate v) { this.releaseTo = v; return this; }
        public Builder runtimeMin(Integer v) { this.runtimeMin = v; return this; }
        public Builder runtimeMax(Integer v) { this.runtimeMax = v; return this; }
        public Builder sort(SortOption v) { this.sort = v; return this; }
        public Builder airDateFrom(LocalDate v) { this.airDateFrom = v; return this; }
        public Builder airDateTo(LocalDate v) { this.airDateTo = v; return this; }
        public Builder theatrical(boolean v) { this.theatrical = v; return this; }
        public Builder page(int v) { this.page = v; return this; }

        public DiscoverQuery build() {
            return new DiscoverQuery(genres, matchAnyGenre, minRating, minVotes, releaseFrom, releaseTo, runtimeMin,
                    runtimeMax, sort, airDateFrom, airDateTo, theatrical, page);
        }
    }
}
