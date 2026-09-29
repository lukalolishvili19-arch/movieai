package com.movieai.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Locale;
import java.util.UUID;

import com.movieai.dto.auth.AuthDtos.AuthResponse;
import com.movieai.dto.auth.AuthDtos.LoginRequest;
import com.movieai.dto.auth.AuthDtos.RegisterRequest;
import com.movieai.dto.auth.AuthDtos.UserProfile;
import com.movieai.entity.RefreshToken;
import com.movieai.entity.User;
import com.movieai.exception.ApiException;
import com.movieai.exception.ErrorCode;
import com.movieai.repository.RefreshTokenRepository;
import com.movieai.repository.UserRepository;
import com.movieai.security.JwtService;
import io.jsonwebtoken.JwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Registration, login and refresh-token rotation. Refresh tokens are single-use: each refresh
 * revokes the presented token and issues a new one in the same family. Presenting a token that
 * was already rotated is treated as theft and revokes the entire family.
 */
@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    public record AuthResult(AuthResponse response, JwtService.IssuedToken refreshToken) {
    }

    private final UserRepository users;
    private final RefreshTokenRepository refreshTokens;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final Clock clock;
    private final String dummyHash;

    public AuthService(UserRepository users, RefreshTokenRepository refreshTokens, PasswordEncoder passwordEncoder,
                       JwtService jwtService, Clock clock) {
        this.users = users;
        this.refreshTokens = refreshTokens;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.clock = clock;
        this.dummyHash = passwordEncoder.encode("movieai-timing-equalizer");
    }

    @Transactional
    public AuthResult register(RegisterRequest request) {
        String email = normalize(request.email());
        if (users.existsByEmail(email)) {
            throw new ApiException(ErrorCode.EMAIL_ALREADY_REGISTERED);
        }
        User user;
        try {
            user = users.saveAndFlush(new User(email, passwordEncoder.encode(request.password())));
        } catch (DataIntegrityViolationException e) {
            throw new ApiException(ErrorCode.EMAIL_ALREADY_REGISTERED);
        }
        return issue(user, UUID.randomUUID());
    }

    @Transactional
    public AuthResult login(LoginRequest request) {
        User user = users.findByEmail(normalize(request.email())).orElse(null);
        if (user == null) {
            passwordEncoder.matches(request.password(), dummyHash);
            throw new ApiException(ErrorCode.INVALID_CREDENTIALS);
        }
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new ApiException(ErrorCode.INVALID_CREDENTIALS);
        }
        return issue(user, UUID.randomUUID());
    }

    @Transactional(noRollbackFor = ApiException.class)
    public AuthResult refresh(String rawToken) {
        JwtService.RefreshClaims claims = parse(rawToken);
        RefreshToken stored = refreshTokens.findById(claims.tokenId())
                .orElseThrow(() -> new ApiException(ErrorCode.INVALID_REFRESH_TOKEN));
        Instant now = clock.instant();
        if (!stored.getTokenHash().equals(hash(rawToken)) || !stored.getUserId().equals(claims.userId())) {
            throw new ApiException(ErrorCode.INVALID_REFRESH_TOKEN);
        }
        if (stored.isRevoked()) {
            int revoked = refreshTokens.revokeFamily(stored.getFamilyId(), now);
            log.warn("Refresh token reuse detected for user {}; revoked {} tokens in family", stored.getUserId(), revoked);
            throw new ApiException(ErrorCode.INVALID_REFRESH_TOKEN);
        }
        if (stored.isExpired(now)) {
            throw new ApiException(ErrorCode.TOKEN_EXPIRED);
        }
        User user = users.findById(stored.getUserId())
                .orElseThrow(() -> new ApiException(ErrorCode.INVALID_REFRESH_TOKEN));
        AuthResult result = issue(user, stored.getFamilyId());
        stored.revoke(now, result.refreshToken().id());
        return result;
    }

    @Transactional
    public void logout(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            return;
        }
        try {
            JwtService.RefreshClaims claims = jwtService.parseRefreshToken(rawToken);
            refreshTokens.findById(claims.tokenId())
                    .filter(t -> t.getTokenHash().equals(hash(rawToken)))
                    .ifPresent(t -> refreshTokens.revokeFamily(t.getFamilyId(), clock.instant()));
        } catch (JwtException | IllegalArgumentException e) {
            // Already invalid; nothing to revoke.
        }
    }

    @Transactional(readOnly = true)
    public UserProfile profile(UUID userId) {
        return users.findById(userId).map(AuthService::toProfile)
                .orElseThrow(() -> new ApiException(ErrorCode.UNAUTHORIZED));
    }

    @Scheduled(cron = "0 17 3 * * *")
    @Transactional
    public void purgeExpiredTokens() {
        int deleted = refreshTokens.deleteExpiredBefore(clock.instant().minus(Duration.ofDays(1)));
        if (deleted > 0) {
            log.info("Purged {} expired refresh tokens", deleted);
        }
    }

    private AuthResult issue(User user, UUID familyId) {
        JwtService.IssuedToken access = jwtService.createAccessToken(user.getId(), user.getEmail());
        JwtService.IssuedToken refresh = jwtService.createRefreshToken(user.getId(), familyId);
        refreshTokens.save(new RefreshToken(refresh.id(), user.getId(), familyId, hash(refresh.token()),
                refresh.expiresAt()));
        AuthResponse response = new AuthResponse(access.token(), "Bearer", jwtService.accessTokenTtlSeconds(),
                toProfile(user));
        return new AuthResult(response, refresh);
    }

    private JwtService.RefreshClaims parse(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new ApiException(ErrorCode.INVALID_REFRESH_TOKEN);
        }
        try {
            return jwtService.parseRefreshToken(rawToken);
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            throw new ApiException(ErrorCode.TOKEN_EXPIRED);
        } catch (JwtException | IllegalArgumentException e) {
            throw new ApiException(ErrorCode.INVALID_REFRESH_TOKEN);
        }
    }

    private static UserProfile toProfile(User user) {
        return new UserProfile(user.getId(), user.getEmail(), user.getCreatedAt());
    }

    private static String normalize(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    static String hash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
