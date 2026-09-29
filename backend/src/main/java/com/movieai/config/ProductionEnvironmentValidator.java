package com.movieai.config;

import java.util.List;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * Under the {@code prod} profile, refuses to start unless every required setting is provided, naming the
 * missing variables (values are never logged). Without this, unresolved placeholders bind as literal text
 * and fail later with misleading errors.
 */
public class ProductionEnvironmentValidator implements EnvironmentPostProcessor, Ordered {

    static final List<String> REQUIRED = List.of(
            "DATABASE_URL", "TMDB_ACCESS_TOKEN", "JWT_SECRET", "JWT_REFRESH_SECRET", "CORS_ALLOWED_ORIGINS");

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (!environment.matchesProfiles("prod")) {
            return;
        }
        List<String> missing = REQUIRED.stream().filter(name -> {
            String value = environment.getProperty(name);
            return value == null || value.isBlank();
        }).toList();
        if (!missing.isEmpty()) {
            throw new IllegalStateException("Missing required environment variables for the prod profile: "
                    + String.join(", ", missing));
        }
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
