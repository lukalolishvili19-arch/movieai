package com.movieai.integration.tmdb;

import java.util.Map;

import com.movieai.exception.ExternalServiceUnavailableException;
import com.movieai.integration.tmdb.model.TmdbModels;
import com.movieai.support.IntegrationTest;
import com.movieai.support.TmdbFixtures;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TmdbClientTest extends IntegrationTest {

    @Autowired
    TmdbClient client;

    @Test
    void sendsBearerTokenLanguageAndBindsSnakeCase() {
        TMDB.on("/tv/1399", 200, TmdbFixtures.TV_DETAILS);

        TmdbModels.TvDetails details = client.get("/tv/1399", Map.of("append_to_response", "credits"),
                TmdbModels.TvDetails.class);

        assertThat(details.numberOfSeasons()).isEqualTo(8);
        assertThat(details.firstAirDate()).isEqualTo("2011-04-17");
        assertThat(details.networks().getFirst().name()).isEqualTo("HBO");
        var request = TMDB.requests("/tv/1399").getFirst();
        assertThat(request.authorization()).isEqualTo("Bearer test-tmdb-token");
        assertThat(request.query()).containsEntry("language", "en-US").containsEntry("append_to_response", "credits");
    }

    @Test
    void notFoundIsTranslated() {
        assertThatThrownBy(() -> client.get("/movie/1", Map.of(), TmdbModels.MovieDetails.class))
                .isInstanceOf(TmdbNotFoundException.class);
    }

    @Test
    void transientFailureIsRetriedOnce() {
        TMDB.once("/movie/2", 429, "{}");
        TMDB.on("/movie/2", 200, TmdbFixtures.MOVIE_DETAILS);

        TmdbModels.MovieDetails details = client.get("/movie/2", Map.of(), TmdbModels.MovieDetails.class);

        assertThat(details.title()).isEqualTo("Dune: Part Two");
        assertThat(TMDB.hits("/movie/2")).isEqualTo(2);
    }

    @Test
    void persistentFailureBecomesUnavailable() {
        TMDB.on("/movie/3", 500, "{}");
        assertThatThrownBy(() -> client.get("/movie/3", Map.of(), TmdbModels.MovieDetails.class))
                .isInstanceOf(ExternalServiceUnavailableException.class)
                .hasMessage("Movie data is temporarily unavailable.");
    }

    @Test
    void rejectedCredentialsAreNotRetried() {
        TMDB.on("/movie/4", 401, "{}");
        assertThatThrownBy(() -> client.get("/movie/4", Map.of(), TmdbModels.MovieDetails.class))
                .isInstanceOf(ExternalServiceUnavailableException.class);
        assertThat(TMDB.hits("/movie/4")).isEqualTo(1);
    }
}
