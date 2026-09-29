package com.movieai.integration.tmdb;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.movieai.config.CacheNames;
import com.movieai.dto.Genre;
import com.movieai.integration.tmdb.model.TmdbModels;
import com.movieai.service.cache.ResilientCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;

/** Genre reference data, cached for a long time because it rarely changes. */
@Service
public class TmdbGenreService {

    private static final Logger log = LoggerFactory.getLogger(TmdbGenreService.class);

    private final TmdbClient client;
    private final ResilientCache cache;

    public TmdbGenreService(TmdbClient client, ResilientCache cache) {
        this.client = client;
        this.cache = cache;
    }

    public List<Genre> movieGenres() {
        return load("movie");
    }

    public List<Genre> tvGenres() {
        return load("tv");
    }

    /** Genre id → name; empty when genres cannot be loaded, so cards still render without names. */
    public Map<Integer, String> movieGenreNames() {
        return names("movie");
    }

    public Map<Integer, String> tvGenreNames() {
        return names("tv");
    }

    private List<Genre> load(String type) {
        return cache.get(CacheNames.REFERENCE, "genres:" + type, () -> {
            TmdbModels.GenreList list = client.get("/genre/" + type + "/list", Map.of(), TmdbModels.GenreList.class);
            return list.genres() == null ? List.<Genre>of()
                    : list.genres().stream().map(g -> new Genre(g.id(), g.name())).toList();
        });
    }

    private Map<Integer, String> names(String type) {
        try {
            return load(type).stream().collect(Collectors.toUnmodifiableMap(Genre::id, Genre::name, (a, b) -> a));
        } catch (RuntimeException e) {
            log.debug("Genre names unavailable for {}: {}", type, e.getMessage());
            return Map.of();
        }
    }
}
