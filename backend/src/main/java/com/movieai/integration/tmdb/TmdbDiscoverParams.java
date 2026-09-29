package com.movieai.integration.tmdb;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.movieai.service.catalog.DiscoverQuery;

/** Translates provider-neutral {@link DiscoverQuery} filters into TMDB /discover parameters. */
final class TmdbDiscoverParams {

    private static final int DEFAULT_MIN_VOTES_FOR_RATING = 200;

    private TmdbDiscoverParams() {
    }

    static Map<String, Object> movie(DiscoverQuery q) {
        Map<String, Object> p = common(q);
        p.put("include_video", "false");
        p.put("primary_release_date.gte", q.releaseFrom());
        p.put("primary_release_date.lte", q.releaseTo());
        if (q.theatrical()) {
            p.put("with_release_type", "2|3");
        }
        DiscoverQuery.SortOption sort = q.sort() == null ? DiscoverQuery.SortOption.POPULARITY : q.sort();
        p.put("sort_by", switch (sort) {
            case POPULARITY -> "popularity.desc";
            case RATING -> "vote_average.desc";
            case RELEASE_DATE -> "primary_release_date.desc";
            case TITLE -> "title.asc";
        });
        applySortGuards(p, q, sort, "primary_release_date.lte");
        return p;
    }

    static Map<String, Object> tv(DiscoverQuery q) {
        Map<String, Object> p = common(q);
        p.put("include_null_first_air_dates", "false");
        p.put("first_air_date.gte", q.releaseFrom());
        p.put("first_air_date.lte", q.releaseTo());
        p.put("air_date.gte", q.airDateFrom());
        p.put("air_date.lte", q.airDateTo());
        DiscoverQuery.SortOption sort = q.sort() == null ? DiscoverQuery.SortOption.POPULARITY : q.sort();
        p.put("sort_by", switch (sort) {
            case POPULARITY -> "popularity.desc";
            case RATING -> "vote_average.desc";
            case RELEASE_DATE -> "first_air_date.desc";
            case TITLE -> "name.asc";
        });
        applySortGuards(p, q, sort, "first_air_date.lte");
        return p;
    }

    private static Map<String, Object> common(DiscoverQuery q) {
        Map<String, Object> p = new LinkedHashMap<>();
        p.put("include_adult", "false");
        p.put("page", q.page());
        if (q.genres() != null && !q.genres().isEmpty()) {
            p.put("with_genres", q.genres().stream().map(String::valueOf)
                    .collect(Collectors.joining(q.matchAnyGenre() ? "|" : ",")));
        }
        if (q.minRating() != null) {
            p.put("vote_average.gte", q.minRating());
        }
        if (q.minVotes() != null) {
            p.put("vote_count.gte", q.minVotes());
        }
        if (q.runtimeMin() != null) {
            p.put("with_runtime.gte", q.runtimeMin());
        }
        if (q.runtimeMax() != null) {
            p.put("with_runtime.lte", q.runtimeMax());
        }
        return p;
    }

    /** Keeps rating/date sorts meaningful: no 10/10 titles with one vote, no far-future placeholders. */
    private static void applySortGuards(Map<String, Object> p, DiscoverQuery q, DiscoverQuery.SortOption sort,
                                        String releaseLteKey) {
        if (sort == DiscoverQuery.SortOption.RATING && q.minVotes() == null) {
            p.put("vote_count.gte", DEFAULT_MIN_VOTES_FOR_RATING);
        }
        if (sort == DiscoverQuery.SortOption.RELEASE_DATE && q.releaseTo() == null) {
            p.put(releaseLteKey, LocalDate.now());
        }
    }
}
