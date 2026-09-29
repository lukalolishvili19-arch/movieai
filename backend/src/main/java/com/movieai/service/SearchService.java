package com.movieai.service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;

import com.movieai.dto.MovieSummary;
import com.movieai.dto.PagedResponse;
import com.movieai.dto.PersonSummary;
import com.movieai.dto.SearchResults;
import com.movieai.dto.TvSummary;
import com.movieai.service.catalog.MovieCatalog;
import com.movieai.service.catalog.PersonCatalog;
import com.movieai.service.catalog.TvCatalog;

import org.springframework.stereotype.Service;

@Service
public class SearchService {

    private final MovieCatalog movies;
    private final TvCatalog tv;
    private final PersonCatalog people;
    private final ExecutorService executor;

    public SearchService(MovieCatalog movies, TvCatalog tv, PersonCatalog people, ExecutorService fanOutExecutor) {
        this.movies = movies;
        this.tv = tv;
        this.people = people;
        this.executor = fanOutExecutor;
    }

    /** Searches all three types concurrently; each type is cached independently. */
    public SearchResults all(String query) {
        CompletableFuture<PagedResponse<MovieSummary>> m = CompletableFuture.supplyAsync(() -> movies.search(query, 1), executor);
        CompletableFuture<PagedResponse<TvSummary>> t = CompletableFuture.supplyAsync(() -> tv.search(query, 1), executor);
        CompletableFuture<PagedResponse<PersonSummary>> p = CompletableFuture.supplyAsync(() -> people.search(query, 1), executor);
        try {
            return new SearchResults(query.trim(), m.join(), t.join(), p.join());
        } catch (CompletionException e) {
            if (e.getCause() instanceof RuntimeException runtime) {
                throw runtime;
            }
            throw e;
        }
    }

    public PagedResponse<MovieSummary> movies(String query, int page) {
        return movies.search(query, page);
    }

    public PagedResponse<TvSummary> tv(String query, int page) {
        return tv.search(query, page);
    }

    public PagedResponse<PersonSummary> people(String query, int page) {
        return people.search(query, page);
    }
}
