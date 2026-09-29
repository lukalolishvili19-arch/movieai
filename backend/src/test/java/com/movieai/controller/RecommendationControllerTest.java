package com.movieai.controller;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.movieai.support.FakeTmdbServer;
import com.movieai.support.IntegrationTest;
import com.movieai.support.TmdbFixtures;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RecommendationControllerTest extends IntegrationTest {

    private void stubPool() {
        TMDB.onQuery("/discover/movie", q -> {
            int page = Integer.parseInt(q.getOrDefault("page", "1"));
            return new FakeTmdbServer.Response(200, TmdbFixtures.discoverPage(page, 20, 2, 40));
        });
    }

    @Test
    void returnsTenPicksAndTranslatesFiltersToDiscovery() throws Exception {
        stubPool();

        mvc.perform(get("/api/v1/recommendations/movies")
                        .param("genres", "28,878")
                        .param("minRating", "7")
                        .param("releasePeriod", "2020-2026")
                        .param("runtime", "under-90"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results", hasSize(10)))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.hasMore").value(true))
                .andExpect(jsonPath("$.totalMatches").value(40));

        var query = TMDB.requests("/discover/movie").getFirst().query();
        assertThat(query)
                .containsEntry("with_genres", "28|878")
                .containsEntry("vote_average.gte", "7.0")
                .containsEntry("primary_release_date.gte", "2020-01-01")
                .containsEntry("primary_release_date.lte", "2026-12-31")
                .containsEntry("with_runtime.lte", "89");
    }

    @Test
    void giveMeTenMoreReturnsANewNonOverlappingBatch() throws Exception {
        stubPool();

        JsonNode first = json(mvc.perform(get("/api/v1/recommendations/movies")).andReturn());
        long seed = first.get("seed").asLong();
        Set<Long> seen = new HashSet<>();
        first.get("results").forEach(m -> seen.add(m.get("id").asLong()));

        JsonNode second = json(mvc.perform(get("/api/v1/recommendations/movies")
                .param("seed", String.valueOf(seed)).param("page", "2")).andReturn());
        assertThat(second.get("results")).hasSize(10);
        second.get("results").forEach(m -> assertThat(seen.add(m.get("id").asLong())).isTrue());

        JsonNode again = json(mvc.perform(get("/api/v1/recommendations/movies")
                .param("seed", String.valueOf(seed)).param("page", "1")).andReturn());
        assertThat(again.get("results")).isEqualTo(first.get("results"));
    }

    @Test
    void noMatchesReturnsEmptyResult() throws Exception {
        TMDB.on("/discover/movie", 200, TmdbFixtures.discoverPage(1, 0, 0, 0));

        mvc.perform(get("/api/v1/recommendations/movies").param("minRating", "9.5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results", hasSize(0)))
                .andExpect(jsonPath("$.hasMore").value(false));
    }

    @Test
    void invalidFiltersAreRejected() throws Exception {
        mvc.perform(get("/api/v1/recommendations/movies").param("releasePeriod", "last-decade"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.details[0].field").value("releasePeriod"));
        mvc.perform(get("/api/v1/recommendations/movies").param("runtime", "forever"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/v1/recommendations/movies").param("limit", "50"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/v1/recommendations/movies").param("minRating", "11"))
                .andExpect(status().isBadRequest());
    }
}
