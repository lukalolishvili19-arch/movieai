package com.movieai.controller;

import jakarta.validation.Valid;

import com.movieai.dto.auth.AuthDtos.AuthResponse;
import com.movieai.dto.auth.AuthDtos.LoginRequest;
import com.movieai.dto.auth.AuthDtos.RegisterRequest;
import com.movieai.exception.ApiException;
import com.movieai.exception.ErrorCode;
import com.movieai.exception.ErrorResponse;
import com.movieai.security.RefreshCookieService;
import com.movieai.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Access tokens are returned in the body and kept in memory by the client. Refresh tokens are
 * only ever sent as an HttpOnly cookie. Cookie-authenticated endpoints (refresh, logout) also
 * require the {@code X-MovieAI-Client} header, which cross-site forms cannot set and which
 * triggers a CORS preflight for cross-origin scripts.
 */
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Registration, login and session refresh. Rate limited per client.")
public class AuthController {

    static final String CLIENT_HEADER = "X-MovieAI-Client";

    private final AuthService auth;
    private final RefreshCookieService cookies;

    public AuthController(AuthService auth, RefreshCookieService cookies) {
        this.auth = auth;
        this.cookies = cookies;
    }

    @PostMapping("/register")
    @Operation(summary = "Create an account", description = "Returns an access token and sets the refresh cookie.")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return respond(HttpStatus.CREATED, auth.register(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Sign in", description = "Returns an access token and sets the refresh cookie.")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return respond(HttpStatus.OK, auth.login(request));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh the session",
            description = "Rotates the refresh cookie and returns a new access token. Reusing an already-rotated "
                    + "refresh token revokes the whole session family.")
    public ResponseEntity<?> refresh(
            @Parameter(in = ParameterIn.HEADER, required = true, description = "Any non-empty value, e.g. 'web'")
            @RequestHeader(name = CLIENT_HEADER, required = false) String clientHeader,
            @Parameter(hidden = true) @CookieValue(name = "${movieai.security.refresh-cookie.name}", required = false)
            String refreshToken) {
        requireClientHeader(clientHeader);
        try {
            return respond(HttpStatus.OK, auth.refresh(refreshToken));
        } catch (ApiException e) {
            return ResponseEntity.status(e.code().status())
                    .header(HttpHeaders.SET_COOKIE, cookies.clear().toString())
                    .cacheControl(CacheControl.noStore())
                    .body(ErrorResponse.of(e.code(), e.getMessage()));
        }
    }

    @PostMapping("/logout")
    @Operation(summary = "Sign out", description = "Revokes the current session and clears the refresh cookie.")
    public ResponseEntity<Void> logout(
            @Parameter(in = ParameterIn.HEADER, required = true)
            @RequestHeader(name = CLIENT_HEADER, required = false) String clientHeader,
            @Parameter(hidden = true) @CookieValue(name = "${movieai.security.refresh-cookie.name}", required = false)
            String refreshToken) {
        requireClientHeader(clientHeader);
        auth.logout(refreshToken);
        return ResponseEntity.noContent().header(HttpHeaders.SET_COOKIE, cookies.clear().toString()).build();
    }

    private ResponseEntity<AuthResponse> respond(HttpStatus status, AuthService.AuthResult result) {
        return ResponseEntity.status(status)
                .header(HttpHeaders.SET_COOKIE,
                        cookies.create(result.refreshToken().token(), result.refreshToken().expiresAt()).toString())
                .cacheControl(CacheControl.noStore())
                .body(result.response());
    }

    private static void requireClientHeader(String value) {
        if (value == null || value.isBlank()) {
            throw new ApiException(ErrorCode.FORBIDDEN, "Missing " + CLIENT_HEADER + " header.");
        }
    }
}
