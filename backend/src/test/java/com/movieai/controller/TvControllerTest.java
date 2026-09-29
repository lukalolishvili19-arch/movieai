package com.movieai.controller;

import com.movieai.support.IntegrationTest;
import com.movieai.support.TmdbFixtures;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TvControllerTest extends IntegrationTest {

    @Test
    void trendingAndAiringTodayUseMatchingTmdbLists() throws Exception {
        TMDB.on("/trending/tv/day", 200, TmdbFixtures.TV_PAGE);
        TMDB.on("/tv/airing_today", 200, TmdbFixtures.TV_PAGE);

        mvc.perform(get("/api/v1/tv/trending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results[0].mediaType").value("tv"))
                .andExpect(jsonPath("$.results[0].title").value("Game of Thrones"))
                .andExpect(jsonPath("$.results[0].year").value(2011))
                .andExpect(jsonPath("$.results[0].genres[1]").value("Sci-Fi & Fantasy"));
        mvc.perform(get("/api/v1/tv/airing-today")).andExpect(status().isOk());

        assertThat(TMDB.hits("/tv/airing_today")).isEqualTo(1);
    }

    @Test
    void detailsExposeRealSeasonDataAndNetworks() throws Exception {
        TMDB.on("/tv/1399", 200, TmdbFixtures.TV_DETAILS);

        mvc.perform(get("/api/v1/tv/1399"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numberOfSeasons").value(8))
                .andExpect(jsonPath("$.numberOfEpisodes").value(73))
                .andExpect(jsonPath("$.status").value("Ended"))
                .andExpect(jsonPath("$.networks[0].name").value("HBO"))
                .andExpect(jsonPath("$.seasons", hasSize(2)))
                .andExpect(jsonPath("$.seasons[1].name").value("Season 1"))
                .andExpect(jsonPath("$.seasons[1].episodeCount").value(10))
                .andExpect(jsonPath("$.cast[0].id").value(22970))
                .andExpect(jsonPath("$.trailer").value(nullValue()))
                .andExpect(jsonPath("$.watchProviders.flatrate", hasSize(0)));
    }

    @Test
    void upcomingTvAlwaysUsesDiscoveryWithFutureFirstAirDate() throws Exception {
        TMDB.on("/discover/tv", 200, TmdbFixtures.TV_PAGE);

        mvc.perform(get("/api/v1/tv").param("category", "upcoming")).andExpect(status().isOk());

        var query = TMDB.requests("/discover/tv").getFirst().query();
        assertThat(query).containsKey("first_air_date.gte").containsEntry("sort_by", "popularity.desc");
    }

    @Test
    void unknownShowReturnsNotFound() throws Exception {
        mvc.perform(get("/api/v1/tv/424242"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("TV_NOT_FOUND"));
    }
}
