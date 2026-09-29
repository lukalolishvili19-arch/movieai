package com.movieai.service;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.IntStream;

import com.movieai.dto.MovieSummary;
import com.movieai.dto.PagedResponse;
import com.movieai.dto.RecommendationResponse;
import com.movieai.service.browse.RuntimeRange;
import com.movieai.service.catalog.DiscoverQuery;
import com.movieai.service.catalog.MovieCatalog;

import org.springframework.stereotype.Service;

/**
 * "What Should I Watch?" picks. Filters become a discovery query; the matching pool (capped to the
 * most popular pages) is split into batches, and a seeded permutation decides which batch each
 * request page returns. The same seed + increasing page therefore yields new, non-overlapping
 * picks, and every underlying provider page is shared through the discovery cache.
 */
@Service
public class RecommendationService {

    static final int PROVIDER_PAGE_SIZE = 20;
    static final int MAX_POOL_PAGES = 15;
    private static final int MIN_VOTES = 100;

    private final MovieCatalog movies;
    private final SecureRandom seedSource = new SecureRandom();

    public RecommendationService(MovieCatalog movies) {
        this.movies = movies;
    }

    public RecommendationResponse movies(List<Integer> genres, Double minRating, Integer yearFrom, Integer yearTo,
                                         RuntimeRange runtime, int page, int limit, Long seed) {
        long effectiveSeed = seed != null ? seed : seedSource.nextLong() & Long.MAX_VALUE;
        DiscoverQuery.Builder base = DiscoverQuery.builder()
                .genres(genres)
                .matchAnyGenre(true)
                .minRating(minRating)
                .minVotes(MIN_VOTES)
                .releaseFrom(yearFrom == null ? null : java.time.LocalDate.of(yearFrom, 1, 1))
                .releaseTo(yearTo == null ? null : java.time.LocalDate.of(yearTo, 12, 31))
                .runtimeMin(runtime.min())
                .runtimeMax(runtime.max())
                .sort(DiscoverQuery.SortOption.POPULARITY);

        PagedResponse<MovieSummary> first = movies.discover(base.page(1).build());
        int poolPages = Math.min(first.totalPages(), MAX_POOL_PAGES);
        int poolSize = Math.min(first.totalResults(), poolPages * PROVIDER_PAGE_SIZE);
        if (poolSize == 0) {
            return new RecommendationResponse(List.of(), page, effectiveSeed, false, 0);
        }
        int totalBatches = (poolSize + limit - 1) / limit;

        List<Integer> order = new ArrayList<>(IntStream.range(0, totalBatches).boxed().toList());
        Collections.shuffle(order, new Random(effectiveSeed));
        int batch = order.get((page - 1) % totalBatches);

        Map<Integer, List<MovieSummary>> shuffledPages = new HashMap<>();
        shuffledPages.put(1, shuffledCopy(first.results(), effectiveSeed ^ 1));
        List<MovieSummary> picks = new ArrayList<>(limit);
        for (int offset = batch * limit; offset < Math.min(poolSize, (batch + 1) * limit); offset++) {
            int providerPage = offset / PROVIDER_PAGE_SIZE + 1;
            List<MovieSummary> pageOrder = shuffledPages.computeIfAbsent(providerPage,
                    p -> shuffledCopy(movies.discover(base.page(p).build()).results(), effectiveSeed ^ p));
            int index = offset % PROVIDER_PAGE_SIZE;
            if (index < pageOrder.size()) {
                picks.add(pageOrder.get(index));
            }
        }
        return new RecommendationResponse(picks, page, effectiveSeed, page < totalBatches, first.totalResults());
    }

    private static List<MovieSummary> shuffledCopy(List<MovieSummary> items, long seed) {
        List<MovieSummary> copy = new ArrayList<>(items);
        Collections.shuffle(copy, new Random(seed));
        return copy;
    }
}
