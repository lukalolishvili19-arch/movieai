package com.movieai.support;

import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.movieai.repository.RefreshTokenRepository;
import com.movieai.repository.UserRepository;
import com.movieai.repository.WatchlistItemRepository;
import org.junit.jupiter.api.BeforeEach;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class IntegrationTest {

    protected static final FakeTmdbServer TMDB = FakeTmdbServer.start();

    @Autowired
    protected MockMvc mvc;
    @Autowired
    protected ObjectMapper objectMapper;
    @Autowired
    protected CacheManager cacheManager;
    @Autowired
    protected WatchlistItemRepository watchlistItems;
    @Autowired
    protected RefreshTokenRepository refreshTokens;
    @Autowired
    protected UserRepository users;

    @DynamicPropertySource
    static void tmdbProperties(DynamicPropertyRegistry registry) {
        registry.add("movieai.tmdb.api-base-url", TMDB::baseUrl);
    }

    @BeforeEach
    void resetState() {
        TMDB.reset();
        cacheManager.getCacheNames().forEach(name -> Objects.requireNonNull(cacheManager.getCache(name)).clear());
        watchlistItems.deleteAll();
        refreshTokens.deleteAll();
        users.deleteAll();
    }

    protected JsonNode json(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    /** Registers a fresh user and returns the access token. */
    protected String registerAndGetToken() throws Exception {
        String email = "user-" + UUID.randomUUID() + "@example.com";
        MvcResult result = mvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"super-secret-pw\"}"))
                .andReturn();
        return json(result).get("accessToken").asText();
    }
}
