package com.movieai.service.browse;

import java.time.Clock;
import java.time.LocalDate;

import com.movieai.dto.MovieSummary;
import com.movieai.dto.PagedResponse;
import com.movieai.dto.TvSummary;
import com.movieai.service.catalog.DiscoverQuery;
import com.movieai.service.catalog.DiscoverQuery.SortOption;
import com.movieai.service.catalog.MovieCatalog;
import com.movieai.service.catalog.TimeWindow;
import com.movieai.service.catalog.TvCatalog;

import org.springframework.stereotype.Service;

/**
 * Server-side browse for the Movies and TV pages. Unfiltered categories use the provider's curated
 * lists (shared cache with the home page); any filter switches to discovery with the category
 * expressed as additional constraints.
 */
@Service
public class BrowseService {

    public enum MovieCategory { TRENDING, POPULAR, TOP_RATED, NOW_PLAYING, UPCOMING }

    public enum TvCategory { TRENDING, POPULAR, TOP_RATED, AIRING_TODAY, ON_THE_AIR, UPCOMING }

    private static final int TOP_RATED_MIN_VOTES = 300;
    private static final int NOW_PLAYING_WINDOW_DAYS = 45;

    private final MovieCatalog movies;
    private final TvCatalog tv;
    private final Clock clock;

    public BrowseService(MovieCatalog movies, TvCatalog tv, Clock clock) {
        this.movies = movies;
        this.tv = tv;
        this.clock = clock;
    }

    public PagedResponse<MovieSummary> movies(MovieCategory category, TimeWindow period, BrowseFilters filters, int page) {
        if (filters.isEmpty()) {
            return switch (category) {
                case TRENDING -> movies.trending(period, page);
                case POPULAR -> movies.popular(page);
                case TOP_RATED -> movies.topRated(page);
                case NOW_PLAYING -> movies.nowPlaying(page);
                case UPCOMING -> movies.upcoming(page);
            };
        }
        LocalDate today = LocalDate.now(clock);
        LocalDate from = filters.from();
        LocalDate to = filters.to();
        SortOption defaultSort = SortOption.POPULARITY;
        Integer minVotes = filters.minRating() != null ? BrowseFilters.MIN_VOTES_WITH_RATING : null;
        boolean theatrical = false;

        switch (category) {
            case TOP_RATED -> {
                defaultSort = SortOption.RATING;
                minVotes = TOP_RATED_MIN_VOTES;
            }
            case NOW_PLAYING -> {
                from = BrowseFilters.later(from, today.minusDays(NOW_PLAYING_WINDOW_DAYS));
                to = BrowseFilters.earlier(to, today);
                theatrical = true;
            }
            case UPCOMING -> from = BrowseFilters.later(from, today.plusDays(1));
            default -> {
                // TRENDING and POPULAR: popularity-sorted discovery
            }
        }
        if (from != null && to != null && from.isAfter(to)) {
            return PagedResponse.empty();
        }
        return movies.discover(DiscoverQuery.builder()
                .genres(filters.genres())
                .minRating(filters.minRating())
                .minVotes(minVotes)
                .releaseFrom(from)
                .releaseTo(to)
                .runtimeMin(filters.runtime() == null ? null : filters.runtime().min())
                .runtimeMax(filters.runtime() == null ? null : filters.runtime().max())
                .sort(filters.sort() == null ? defaultSort : filters.sort())
                .theatrical(theatrical)
                .page(page)
                .build());
    }

    public PagedResponse<TvSummary> tv(TvCategory category, TimeWindow period, BrowseFilters filters, int page) {
        if (filters.isEmpty() && category != TvCategory.UPCOMING) {
            return switch (category) {
                case TRENDING -> tv.trending(period, page);
                case POPULAR -> tv.popular(page);
                case TOP_RATED -> tv.topRated(page);
                case AIRING_TODAY -> tv.airingToday(page);
                case ON_THE_AIR -> tv.onTheAir(page);
                case UPCOMING -> throw new IllegalStateException("unreachable");
            };
        }
        LocalDate today = LocalDate.now(clock);
        LocalDate from = filters.from();
        LocalDate to = filters.to();
        LocalDate airFrom = null;
        LocalDate airTo = null;
        SortOption defaultSort = SortOption.POPULARITY;
        Integer minVotes = filters.minRating() != null ? BrowseFilters.MIN_VOTES_WITH_RATING : null;

        switch (category) {
            case TOP_RATED -> {
                defaultSort = SortOption.RATING;
                minVotes = TOP_RATED_MIN_VOTES;
            }
            case AIRING_TODAY -> {
                airFrom = today;
                airTo = today;
            }
            case ON_THE_AIR -> {
                airFrom = today;
                airTo = today.plusDays(7);
            }
            case UPCOMING -> from = BrowseFilters.later(from, today.plusDays(1));
            default -> {
                // TRENDING and POPULAR
            }
        }
        if (from != null && to != null && from.isAfter(to)) {
            return PagedResponse.empty();
        }
        return tv.discover(DiscoverQuery.builder()
                .genres(filters.genres())
                .minRating(filters.minRating())
                .minVotes(minVotes)
                .releaseFrom(from)
                .releaseTo(to)
                .airDateFrom(airFrom)
                .airDateTo(airTo)
                .runtimeMin(filters.runtime() == null ? null : filters.runtime().min())
                .runtimeMax(filters.runtime() == null ? null : filters.runtime().max())
                .sort(filters.sort() == null ? defaultSort : filters.sort())
                .page(page)
                .build());
    }
}
