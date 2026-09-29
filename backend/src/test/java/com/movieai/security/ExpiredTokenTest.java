package com.movieai.security;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import com.movieai.config.MovieAiProperties;
import com.movieai.support.IntegrationTest;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ExpiredTokenTest extends IntegrationTest {

    @Autowired
    MovieAiProperties properties;

    @Test
    void expiredAccessTokenIsReportedAsTokenExpired() throws Exception {
        Clock past = Clock.fixed(Instant.now().minus(Duration.ofHours(2)), ZoneOffset.UTC);
        String expired = new JwtService(properties, past).createAccessToken(UUID.randomUUID(), "a@b.c").token();

        mvc.perform(get("/api/v1/watchlist").header("Authorization", "Bearer " + expired))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("TOKEN_EXPIRED"));
    }
}
