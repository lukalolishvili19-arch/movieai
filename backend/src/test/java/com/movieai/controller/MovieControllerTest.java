package com.movieai.controller;

import java.util.Objects;

import com.movieai.config.CacheNames;
import com.movieai.support.IntegrationTest;
import com.movieai.support.TmdbFixtures;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MovieControllerTest extends IntegrationTest {

    @Test
    void trendingReturnsNormalizedSummariesWithoutInventedValues() throws Exception {
        TMDB.on("/trending/movie/week", 200, TmdbFixtures.TRENDING_MOVIES);

        mvc.perform(get("/api/v1/movies/trending").param("period", "week"))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", containsString("public")))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.totalPages").value(3))
                .andExpect(jsonPath("$.results", hasSize(2))) // adult title filtered out
                .andExpect(jsonPath("$.results[0].id").value(693134))
                .andExpect(jsonPath("$.results[0].mediaType").value("movie"))
                .andExpect(jsonPath("$.results[0].title").value("Dune: Part Two"))
                .andExpect(jsonPath("$.results[0].year").value(2024))
                .andExpect(jsonPath("$.results[0].rating").value(8.2))
                .andExpect(jsonPath("$.results[0].genres[0]").value("Science Fiction"))
                .andExpect(jsonPath("$.results[0].posterUrl").value("https://image.tmdb.org/t/p/w342/dune2.jpg"))
                .andExpect(jsonPath("$.results[0].backdropUrl").value("https://image.tmdb.org/t/p/w1280/dune2-bg.jpg"))
                .andExpect(jsonPath("$.results[1].rating").value(nullValue()))
                .andExpect(jsonPath("$.results[1].year").value(nullValue()))
                .andExpect(jsonPath("$.results[1].releaseDate").value(nullValue()))
                .andExpect(jsonPath("$.results[1].posterUrl").value(nullValue()));

        var request = TMDB.requests("/trending/movie/week").getFirst();
        assertThat(request.authorization()).isEqualTo("Bearer test-tmdb-token");
        assertThat(request.query()).containsEntry("language", "en-US").containsEntry("page", "1");
    }

    @Test
    void publicListsAreCachedAndDoNotRepeatUpstreamCalls() throws Exception {
        TMDB.on("/movie/now_playing", 200, TmdbFixtures.TRENDING_MOVIES);

        mvc.perform(get("/api/v1/movies/now-playing")).andExpect(status().isOk());
        mvc.perform(get("/api/v1/movies/now-playing")).andExpect(status().isOk());

        assertThat(TMDB.hits("/movie/now_playing")).isEqualTo(1);
    }

    @Test
    void detailsIncludeCreditsTrailerProvidersAndSimilarFromOneUpstreamCall() throws Exception {
        TMDB.on("/movie/693134", 200, TmdbFixtures.MOVIE_DETAILS);

        mvc.perform(get("/api/v1/movies/693134").param("region", "us"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Dune: Part Two"))
                .andExpect(jsonPath("$.originalTitle").value("Dune: Part Two"))
                .andExpect(jsonPath("$.releaseDate").value("2024-02-27"))
                .andExpect(jsonPath("$.runtime").value(167))
                .andExpect(jsonPath("$.genres[0].name").value("Science Fiction"))
                .andExpect(jsonPath("$.directors[0].name").value("Denis Villeneuve"))
                .andExpect(jsonPath("$.crew", hasSize(1)))
                .andExpect(jsonPath("$.cast[0].id").value(1190668))
                .andExpect(jsonPath("$.cast[0].character").value("Paul Atreides"))
                .andExpect(jsonPath("$.cast[1].profileUrl").value(nullValue()))
                .andExpect(jsonPath("$.trailer.key").value("Way9Dexny3w"))
                .andExpect(jsonPath("$.trailer.embedUrl").value("https://www.youtube-nocookie.com/embed/Way9Dexny3w"))
                .andExpect(jsonPath("$.videos[0].type").value("Trailer"))
                .andExpect(jsonPath("$.images.backdrops[0].url").value("https://image.tmdb.org/t/p/w780/bd1.jpg"))
                .andExpect(jsonPath("$.similar[0].id").value(438631))
                .andExpect(jsonPath("$.watchProviders.region").value("US"))
                .andExpect(jsonPath("$.watchProviders.flatrate[0].name").value("Max"))
                .andExpect(jsonPath("$.watchProviders.attribution").value("JustWatch"));

        mvc.perform(get("/api/v1/movies/693134/watch-providers").param("region", "GB"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.buy[0].name").value("Amazon Video"));
        mvc.perform(get("/api/v1/movies/693134/videos")).andExpect(jsonPath("$", hasSize(3)));
        mvc.perform(get("/api/v1/movies/693134/images")).andExpect(jsonPath("$.posters", hasSize(1)));

        assertThat(TMDB.hits("/movie/693134")).isEqualTo(1);
        assertThat(TMDB.requests("/movie/693134").getFirst().query().get("append_to_response"))
                .contains("credits", "videos", "images", "watch/providers");
    }

    @Test
    void regionWithoutProvidersReturnsEmptyProviderLists() throws Exception {
        TMDB.on("/movie/693134", 200, TmdbFixtures.MOVIE_DETAILS);

        mvc.perform(get("/api/v1/movies/693134/watch-providers").param("region", "DE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.region").value("DE"))
                .andExpect(jsonPath("$.flatrate", hasSize(0)))
                .andExpect(jsonPath("$.link").value(nullValue()));
    }

    @Test
    void unknownMovieReturnsNotFoundEnvelope() throws Exception {
        mvc.perform(get("/api/v1/movies/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("MOVIE_NOT_FOUND"));
    }

    @Test
    void tmdbOutageReturnsCleanErrorWithoutInternals() throws Exception {
        TMDB.on("/movie/upcoming", 503, "{\"status_message\":\"internal upstream detail\"}");

        mvc.perform(get("/api/v1/movies/upcoming"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(header().string("Cache-Control", not(containsString("public"))))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("TMDB_UNAVAILABLE"))
                .andExpect(jsonPath("$.error.message").value("Movie data is temporarily unavailable."))
                .andExpect(content().string(not(containsString("upstream"))))
                .andExpect(content().string(not(containsString("test-tmdb-token"))));

        assertThat(TMDB.hits("/movie/upcoming")).isEqualTo(2); // one retry
    }

    @Test
    void staleCacheIsServedWhenTmdbIsDownAfterFreshEntryExpired() throws Exception {
        TMDB.on("/movie/top_rated", 200, TmdbFixtures.TRENDING_MOVIES);
        mvc.perform(get("/api/v1/movies/top-rated")).andExpect(status().isOk());

        Objects.requireNonNull(cacheManager.getCache(CacheNames.LISTS)).clear(); // simulate TTL expiry
        TMDB.on("/movie/top_rated", 500, "{}");

        mvc.perform(get("/api/v1/movies/top-rated"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results[0].title").value("Dune: Part Two"));
    }

    @Test
    void browseWithFiltersUsesServerSideDiscovery() throws Exception {
        TMDB.on("/discover/movie", 200, TmdbFixtures.discoverPage(2, 20, 10, 200));

        mvc.perform(get("/api/v1/movies")
                        .param("category", "top-rated")
                        .param("genres", "28,12")
                        .param("minRating", "7.5")
                        .param("yearFrom", "2010")
                        .param("yearTo", "2020")
                        .param("runtime", "90-120")
                        .param("page", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(2))
                .andExpect(jsonPath("$.results", hasSize(20)));

        var query = TMDB.requests("/discover/movie").getFirst().query();
        assertThat(query)
                .containsEntry("with_genres", "28,12")
                .containsEntry("vote_average.gte", "7.5")
                .containsEntry("vote_count.gte", "300")
                .containsEntry("primary_release_date.gte", "2010-01-01")
                .containsEntry("primary_release_date.lte", "2020-12-31")
                .containsEntry("with_runtime.gte", "90")
                .containsEntry("with_runtime.lte", "120")
                .containsEntry("sort_by", "vote_average.desc")
                .containsEntry("include_adult", "false")
                .containsEntry("page", "2");
    }

    @Test
    void browseWithoutFiltersUsesCuratedList() throws Exception {
        TMDB.on("/movie/popular", 200, TmdbFixtures.TRENDING_MOVIES);

        mvc.perform(get("/api/v1/movies").param("category", "popular")).andExpect(status().isOk());

        assertThat(TMDB.hits("/movie/popular")).isEqualTo(1);
        assertThat(TMDB.hits("/discover/movie")).isZero();
    }

    @Test
    void invalidParametersAreRejected() throws Exception {
        mvc.perform(get("/api/v1/movies/trending").param("period", "month"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.error.details[0].field").value("period"));
        mvc.perform(get("/api/v1/movies/popular").param("page", "501"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
        mvc.perform(get("/api/v1/movies").param("category", "nope"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/v1/movies/693134").param("region", "USA"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/v1/movies/abc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void genresAreServedFromReferenceData() throws Exception {
        mvc.perform(get("/api/v1/movies/genres"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(28))
                .andExpect(jsonPath("$[0].name").value("Action"));
    }
}
