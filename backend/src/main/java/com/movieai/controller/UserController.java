package com.movieai.controller;

import com.movieai.dto.auth.AuthDtos.UserProfile;
import com.movieai.security.AuthenticatedUser;
import com.movieai.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Users", description = "The signed-in user's profile. Requires a Bearer access token.")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final AuthService auth;

    public UserController(AuthService auth) {
        this.auth = auth;
    }

    @GetMapping("/me")
    @Operation(summary = "Current user profile")
    public ResponseEntity<UserProfile> me(@AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore().cachePrivate()).body(auth.profile(user.id()));
    }
}
