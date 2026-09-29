package com.movieai.security;

import com.movieai.support.IntegrationTest;
import org.junit.jupiter.api.Test;

import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestPropertySource(properties = "movieai.rate-limit.auth-per-minute=3")
class RateLimitTest extends IntegrationTest {

    @Test
    void authenticationAttemptsAreRateLimited() throws Exception {
        String body = "{\"email\":\"x@example.com\",\"password\":\"wrong-password\"}";
        for (int i = 0; i < 3; i++) {
            mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isUnauthorized());
        }
        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string("Retry-After", notNullValue()))
                .andExpect(jsonPath("$.error.code").value("RATE_LIMITED"));
    }
}
