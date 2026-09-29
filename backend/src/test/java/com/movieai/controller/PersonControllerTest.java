package com.movieai.controller;

import com.movieai.support.IntegrationTest;
import com.movieai.support.TmdbFixtures;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PersonControllerTest extends IntegrationTest {

    @Test
    void trendingPeopleIncludeKnownForTitles() throws Exception {
        TMDB.on("/trending/person/week", 200, TmdbFixtures.PEOPLE_PAGE);

        mvc.perform(get("/api/v1/people/trending").param("period", "week"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results[0].name").value("Timothée Chalamet"))
                .andExpect(jsonPath("$.results[0].knownFor[0]").value("Dune: Part Two"))
                .andExpect(jsonPath("$.results[0].knownFor[1]").value("Homeland"))
                .andExpect(jsonPath("$.results[0].profileUrl").value("https://image.tmdb.org/t/p/w185/tc.jpg"));
    }

    @Test
    void detailsNeverFabricateMissingBiographyData() throws Exception {
        TMDB.on("/person/1190668", 200, TmdbFixtures.PERSON_DETAILS);

        mvc.perform(get("/api/v1/people/1190668"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.biography").value(nullValue()))
                .andExpect(jsonPath("$.birthday").value(nullValue()))
                .andExpect(jsonPath("$.placeOfBirth").value(nullValue()))
                .andExpect(jsonPath("$.knownForDepartment").value("Acting"))
                .andExpect(jsonPath("$.profileUrl").value("https://image.tmdb.org/t/p/h632/tc.jpg"))
                .andExpect(jsonPath("$.images", hasSize(2)));
    }

    @Test
    void creditsAreSplitDeduplicatedAndExcludeTalkShows() throws Exception {
        TMDB.on("/person/1190668", 200, TmdbFixtures.PERSON_DETAILS);

        mvc.perform(get("/api/v1/people/1190668/credits"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.movies", hasSize(2)))
                .andExpect(jsonPath("$.movies[0].title").value("Dune: Part Two"))
                .andExpect(jsonPath("$.movies[0].character").value("Paul Atreides"))
                .andExpect(jsonPath("$.movies[0].job").value("Executive Producer"))
                .andExpect(jsonPath("$.movies[0].creditType").value("cast"))
                .andExpect(jsonPath("$.tv", hasSize(1)))
                .andExpect(jsonPath("$.tv[0].title").value("Homeland"))
                .andExpect(jsonPath("$.tv[0].episodeCount").value(8));

        mvc.perform(get("/api/v1/people/1190668/images"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[1].thumbnailUrl").value("https://image.tmdb.org/t/p/w185/tc2.jpg"));
    }

    @Test
    void unknownPersonReturnsNotFound() throws Exception {
        mvc.perform(get("/api/v1/people/77777777"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("PERSON_NOT_FOUND"));
    }
}
