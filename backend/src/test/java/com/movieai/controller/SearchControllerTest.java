package com.movieai.controller;

import com.movieai.support.IntegrationTest;
import com.movieai.support.TmdbFixtures;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SearchControllerTest extends IntegrationTest {

    @Test
    void searchAllReturnsEachMediaTypeSection() throws Exception {
        TMDB.on("/search/movie", 200, TmdbFixtures.TRENDING_MOVIES);
        TMDB.on("/search/tv", 200, TmdbFixtures.TV_PAGE);
        TMDB.on("/search/person", 200, TmdbFixtures.PEOPLE_PAGE);

        mvc.perform(get("/api/v1/search").param("query", "  dune "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.query").value("dune"))
                .andExpect(jsonPath("$.movies.results[0].title").value("Dune: Part Two"))
                .andExpect(jsonPath("$.tv.results[0].title").value("Game of Thrones"))
                .andExpect(jsonPath("$.people.results[0].name").value("Timothée Chalamet"));

        assertThat(TMDB.requests("/search/movie").getFirst().query())
                .containsEntry("query", "dune")
                .containsEntry("include_adult", "false");
    }

    @Test
    void typedSearchSupportsPagination() throws Exception {
        TMDB.on("/search/tv", 200, TmdbFixtures.TV_PAGE);

        mvc.perform(get("/api/v1/search/tv").param("query", "thrones").param("page", "3"))
                .andExpect(status().isOk());

        assertThat(TMDB.requests("/search/tv").getFirst().query()).containsEntry("page", "3");
    }

    @Test
    void identicalSearchesShareTheCache() throws Exception {
        TMDB.on("/search/person", 200, TmdbFixtures.PEOPLE_PAGE);

        mvc.perform(get("/api/v1/search/people").param("query", "Chalamet")).andExpect(status().isOk());
        mvc.perform(get("/api/v1/search/people").param("query", "chalamet")).andExpect(status().isOk());

        assertThat(TMDB.hits("/search/person")).isEqualTo(1);
    }

    @Test
    void blankOrMissingQueryIsRejected() throws Exception {
        mvc.perform(get("/api/v1/search").param("query", "   "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
        mvc.perform(get("/api/v1/search/movies"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.details[0].field").value("query"));
        mvc.perform(get("/api/v1/search/movies").param("query", "x".repeat(101)))
                .andExpect(status().isBadRequest());
    }
}
