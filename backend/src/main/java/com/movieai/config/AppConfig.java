package com.movieai.config;

import java.time.Clock;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    /** Used for fan-out calls (combined search, watchlist enrichment). */
    @Bean(destroyMethod = "close")
    public ExecutorService fanOutExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }
}
