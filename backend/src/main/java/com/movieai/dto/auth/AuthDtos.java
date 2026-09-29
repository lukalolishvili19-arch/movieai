package com.movieai.dto.auth;

import java.time.Instant;
import java.util.UUID;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;

public final class AuthDtos {

    private AuthDtos() {
    }

    public record RegisterRequest(
            @Schema(example = "jane@example.com")
            @NotBlank @Email @Size(max = 320) String email,
            @Schema(description = "8 to 72 characters", example = "correct-horse-battery")
            @NotBlank @Size(min = 8, max = 72) String password) {
    }

    public record LoginRequest(
            @Schema(example = "jane@example.com") @NotBlank @Email @Size(max = 320) String email,
            @NotBlank @Size(max = 72) String password) {
    }

    @Schema(description = "Short-lived access token. The refresh token is set as an HttpOnly cookie.")
    public record AuthResponse(
            String accessToken,
            @Schema(example = "Bearer") String tokenType,
            @Schema(description = "Access token lifetime in seconds", example = "900") long expiresIn,
            UserProfile user) {
    }

    public record UserProfile(
            UUID id,
            @Schema(example = "jane@example.com") String email,
            Instant createdAt) {
    }
}
