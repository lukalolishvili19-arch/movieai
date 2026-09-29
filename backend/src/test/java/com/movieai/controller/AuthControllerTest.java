package com.movieai.controller;

import jakarta.servlet.http.Cookie;

import com.movieai.support.IntegrationTest;
import org.junit.jupiter.api.Test;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerTest extends IntegrationTest {

    private static final String BODY = "{\"email\":\"Jane@Example.com\",\"password\":\"super-secret-pw\"}";

    private MvcResult register() throws Exception {
        return mvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isCreated())
                .andReturn();
    }

    private static Cookie refreshCookie(MvcResult result) {
        Cookie cookie = result.getResponse().getCookie("movieai_refresh");
        assertThat(cookie).isNotNull();
        return cookie;
    }

    @Test
    void registerReturnsAccessTokenAndHttpOnlyRefreshCookie() throws Exception {
        MvcResult result = register();

        assertThat(json(result).get("accessToken").asText()).isNotBlank();
        assertThat(json(result).get("tokenType").asText()).isEqualTo("Bearer");
        assertThat(json(result).get("expiresIn").asLong()).isEqualTo(900);
        assertThat(json(result).get("user").get("email").asText()).isEqualTo("jane@example.com");
        String setCookie = result.getResponse().getHeader("Set-Cookie");
        assertThat(setCookie).contains("HttpOnly").contains("SameSite=Lax").contains("Path=/api/v1/auth");
        assertThat(users.findByEmail("jane@example.com").orElseThrow().getPasswordHash())
                .startsWith("$2").doesNotContain("super-secret-pw");
    }

    @Test
    void duplicateEmailIsRejectedCaseInsensitively() throws Exception {
        register();
        mvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"jane@example.COM\",\"password\":\"another-password\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("EMAIL_ALREADY_REGISTERED"));
    }

    @Test
    void registrationValidatesInput() throws Exception {
        mvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"not-an-email\",\"password\":\"short\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.error.details.length()").value(2));
    }

    @Test
    void loginSucceedsWithCorrectPasswordOnly() throws Exception {
        register();
        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty());
        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"jane@example.com\",\"password\":\"wrong-password\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("INVALID_CREDENTIALS"));
        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"nobody@example.com\",\"password\":\"whatever-123\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("INVALID_CREDENTIALS"));
    }

    @Test
    void refreshRotatesTokenAndDetectsReuse() throws Exception {
        Cookie original = refreshCookie(register());

        MvcResult rotated = mvc.perform(post("/api/v1/auth/refresh").header("X-MovieAI-Client", "web").cookie(original))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andReturn();
        Cookie next = refreshCookie(rotated);
        assertThat(next.getValue()).isNotEqualTo(original.getValue());

        // Replaying the rotated-out token revokes the whole family...
        mvc.perform(post("/api/v1/auth/refresh").header("X-MovieAI-Client", "web").cookie(original))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("INVALID_REFRESH_TOKEN"))
                .andExpect(header().string("Set-Cookie", containsString("Max-Age=0")));
        // ...including the legitimately rotated token.
        mvc.perform(post("/api/v1/auth/refresh").header("X-MovieAI-Client", "web").cookie(next))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refreshRequiresClientHeaderAndCookie() throws Exception {
        Cookie cookie = refreshCookie(register());
        mvc.perform(post("/api/v1/auth/refresh").cookie(cookie))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("FORBIDDEN"));
        mvc.perform(post("/api/v1/auth/refresh").header("X-MovieAI-Client", "web"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("INVALID_REFRESH_TOKEN"));
        mvc.perform(post("/api/v1/auth/refresh").header("X-MovieAI-Client", "web")
                        .cookie(new Cookie("movieai_refresh", "garbage")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logoutRevokesSession() throws Exception {
        Cookie cookie = refreshCookie(register());
        mvc.perform(post("/api/v1/auth/logout").header("X-MovieAI-Client", "web").cookie(cookie))
                .andExpect(status().isNoContent())
                .andExpect(header().string("Set-Cookie", containsString("Max-Age=0")));
        mvc.perform(post("/api/v1/auth/refresh").header("X-MovieAI-Client", "web").cookie(cookie))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void profileRequiresValidAccessToken() throws Exception {
        String token = json(register()).get("accessToken").asText();

        mvc.perform(get("/api/v1/users/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("jane@example.com"))
                .andExpect(header().string("Cache-Control", containsString("no-store")));
        mvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
        mvc.perform(get("/api/v1/users/me").header("Authorization", "Bearer not.a.jwt"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
    }

    @Test
    void publicEndpointsIgnoreInvalidTokens() throws Exception {
        mvc.perform(get("/api/v1/movies/genres").header("Authorization", "Bearer broken"))
                .andExpect(status().isOk());
    }

    @Test
    void securityHeadersArePresent() throws Exception {
        mvc.perform(get("/api/v1/movies/genres"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string("X-Frame-Options", "DENY"))
                .andExpect(header().string("Referrer-Policy", "no-referrer"))
                .andExpect(header().string("Content-Security-Policy", containsString("default-src 'none'")));
    }
}
