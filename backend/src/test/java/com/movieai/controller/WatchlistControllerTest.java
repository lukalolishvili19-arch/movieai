package com.movieai.controller;

import com.movieai.support.IntegrationTest;
import com.movieai.support.TmdbFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class WatchlistControllerTest extends IntegrationTest {

    private String token;

    @BeforeEach
    void setUp() throws Exception {
        TMDB.on("/movie/693134", 200, TmdbFixtures.MOVIE_DETAILS);
        TMDB.on("/tv/1399", 200, TmdbFixtures.TV_DETAILS);
        token = registerAndGetToken();
    }

    private String bearer() {
        return "Bearer " + token;
    }

    private void add(String type, long id, int expectedStatus) throws Exception {
        mvc.perform(post("/api/v1/watchlist").header("Authorization", bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"mediaType\":\"" + type + "\",\"tmdbId\":" + id + "}"))
                .andExpect(status().is(expectedStatus));
    }

    @Test
    void watchlistRequiresAuthentication() throws Exception {
        mvc.perform(get("/api/v1/watchlist"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
        mvc.perform(post("/api/v1/watchlist").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"mediaType\":\"movie\",\"tmdbId\":1}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void addIsIdempotentAndPersistsMovieAndTv() throws Exception {
        add("movie", 693134, 201);
        add("movie", 693134, 200);
        add("tv", 1399, 201);

        assertThat(watchlistItems.count()).isEqualTo(2);

        mvc.perform(get("/api/v1/watchlist").header("Authorization", bearer()))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", containsString("no-store")))
                .andExpect(jsonPath("$.totalResults").value(2))
                .andExpect(jsonPath("$.results[0].mediaType").value("tv"))
                .andExpect(jsonPath("$.results[0].media.title").value("Game of Thrones"))
                .andExpect(jsonPath("$.results[1].media.title").value("Dune: Part Two"))
                .andExpect(jsonPath("$.results[1].media.posterUrl").value("https://image.tmdb.org/t/p/w342/dune2.jpg"));

        mvc.perform(get("/api/v1/watchlist").param("mediaType", "movie").header("Authorization", bearer()))
                .andExpect(jsonPath("$.results", hasSize(1)));

        mvc.perform(get("/api/v1/watchlist/ids").header("Authorization", bearer()))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[1].mediaType").value("movie"))
                .andExpect(jsonPath("$[1].tmdbId").value(693134));
    }

    @Test
    void checkAndRemove() throws Exception {
        add("movie", 693134, 201);

        mvc.perform(get("/api/v1/watchlist/check/movie/693134").header("Authorization", bearer()))
                .andExpect(jsonPath("$.inWatchlist").value(true));
        mvc.perform(delete("/api/v1/watchlist/movie/693134").header("Authorization", bearer()))
                .andExpect(status().isNoContent());
        mvc.perform(delete("/api/v1/watchlist/movie/693134").header("Authorization", bearer()))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/v1/watchlist/check/movie/693134").header("Authorization", bearer()))
                .andExpect(jsonPath("$.inWatchlist").value(false));
    }

    @Test
    void usersOnlySeeTheirOwnWatchlist() throws Exception {
        add("movie", 693134, 201);
        String other = registerAndGetToken();

        mvc.perform(get("/api/v1/watchlist").header("Authorization", "Bearer " + other))
                .andExpect(jsonPath("$.results", hasSize(0)));
        mvc.perform(get("/api/v1/watchlist/check/movie/693134").header("Authorization", "Bearer " + other))
                .andExpect(jsonPath("$.inWatchlist").value(false));
    }

    @Test
    void cannotAddTitlesThatDoNotExist() throws Exception {
        add("movie", 424242, 404);
        assertThat(watchlistItems.count()).isZero();
    }

    @Test
    void itemsStayListedWhenTmdbIsUnavailable() throws Exception {
        add("movie", 693134, 201);
        cacheManager.getCacheNames().forEach(n -> cacheManager.getCache(n).clear());
        TMDB.on("/movie/693134", 503, "{}");

        mvc.perform(get("/api/v1/watchlist").header("Authorization", bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results[0].tmdbId").value(693134))
                .andExpect(jsonPath("$.results[0].media").value(nullValue()));
    }

    @Test
    void invalidInputIsRejected() throws Exception {
        mvc.perform(post("/api/v1/watchlist").header("Authorization", bearer())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"mediaType\":\"book\",\"tmdbId\":5}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
        mvc.perform(post("/api/v1/watchlist").header("Authorization", bearer())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"mediaType\":\"movie\",\"tmdbId\":-1}"))
                .andExpect(status().isBadRequest());
        mvc.perform(delete("/api/v1/watchlist/book/5").header("Authorization", bearer()))
                .andExpect(status().isBadRequest());
    }
}
