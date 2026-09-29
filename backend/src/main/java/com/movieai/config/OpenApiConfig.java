package com.movieai.config;

import java.util.Map;

import com.movieai.exception.ErrorResponse;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String ERROR_REF = "#/components/schemas/ErrorResponse";

    @Bean
    public OpenAPI movieAiOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("MovieAI API")
                        .version("v1")
                        .description("""
                                Movie, TV and people discovery backed by TMDB, plus persistent user watchlists.

                                **Authentication.** Public catalog, search and recommendation endpoints need no \
                                authentication. Watchlist and user endpoints need `Authorization: Bearer <accessToken>` \
                                obtained from `/api/v1/auth/login` or `/register`. Access tokens expire after 15 minutes; \
                                call `/api/v1/auth/refresh` (with the HttpOnly refresh cookie and the `X-MovieAI-Client` \
                                header) to rotate the session.

                                **Errors.** Every failure uses the same envelope: \
                                `{"success": false, "error": {"code": "TMDB_UNAVAILABLE", "message": "..."}}`. \
                                Codes: VALIDATION_ERROR (400), UNAUTHORIZED / TOKEN_EXPIRED / INVALID_CREDENTIALS / \
                                INVALID_REFRESH_TOKEN (401), FORBIDDEN (403), MOVIE_NOT_FOUND / TV_NOT_FOUND / \
                                PERSON_NOT_FOUND / NOT_FOUND (404), EMAIL_ALREADY_REGISTERED (409), RATE_LIMITED (429, \
                                with Retry-After), INTERNAL_ERROR (500), TMDB_UNAVAILABLE (503).

                                **Attribution.** This product uses the TMDB API but is not endorsed or certified by TMDB. \
                                Watch-provider data is provided by JustWatch.""")
                        .license(new License().name("Data: TMDB").url("https://www.themoviedb.org/")))
                .components(new Components().addSecuritySchemes("bearerAuth", new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")));
    }

    /** Documents the shared error responses on every operation. */
    @Bean
    public OpenApiCustomizer errorResponsesCustomizer() {
        return openApi -> {
            Map<String, Schema> schemas = ModelConverters.getInstance().readAll(ErrorResponse.class);
            schemas.forEach(openApi.getComponents()::addSchemas);
            openApi.getPaths().forEach((path, item) -> item.readOperations().forEach(op -> {
                boolean secured = path.startsWith("/api/v1/watchlist") || path.startsWith("/api/v1/users");
                boolean publicCatalog = !secured && !path.startsWith("/api/v1/auth");
                add(op, "400", "Invalid request parameters (VALIDATION_ERROR)");
                add(op, "429", "Rate limit exceeded (RATE_LIMITED)");
                add(op, "500", "Unexpected server error (INTERNAL_ERROR)");
                if (secured) {
                    add(op, "401", "Missing, invalid or expired access token (UNAUTHORIZED / TOKEN_EXPIRED)");
                }
                if (publicCatalog || path.startsWith("/api/v1/watchlist")) {
                    add(op, "503", "TMDB is temporarily unavailable and no cached copy exists (TMDB_UNAVAILABLE)");
                }
                if (path.contains("{id}") || path.contains("{tmdbId}")) {
                    add(op, "404", "Unknown id (MOVIE_NOT_FOUND / TV_NOT_FOUND / PERSON_NOT_FOUND)");
                }
            }));
        };
    }

    private static void add(Operation op, String status, String description) {
        ApiResponses responses = op.getResponses();
        if (responses.containsKey(status)) {
            return;
        }
        responses.addApiResponse(status, new ApiResponse().description(description).content(new Content()
                .addMediaType("application/json", new MediaType().schema(new Schema<>().$ref(ERROR_REF)))));
    }
}
