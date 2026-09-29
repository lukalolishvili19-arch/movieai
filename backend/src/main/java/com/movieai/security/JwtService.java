package com.movieai.security;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;

import com.movieai.config.MovieAiProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;

/**
 * Issues and validates HS256 JWTs. Access and refresh tokens use different secrets and a
 * {@code typ} claim so one can never be used in place of the other.
 */
@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);
    private static final int MIN_SECRET_BYTES = 32;
    private static final String TYPE_CLAIM = "typ";
    private static final String FAMILY_CLAIM = "fam";

    private final SecretKey accessKey;
    private final SecretKey refreshKey;
    private final MovieAiProperties.Jwt properties;
    private final Clock clock;

    public JwtService(MovieAiProperties properties, Clock clock) {
        this.properties = properties.security().jwt();
        this.clock = clock;
        this.accessKey = key(this.properties.secret(), "JWT_SECRET");
        this.refreshKey = key(this.properties.refreshSecret(), "JWT_REFRESH_SECRET");
    }

    public record IssuedToken(String token, UUID id, Instant expiresAt) {
    }

    public record RefreshClaims(UUID tokenId, UUID userId, UUID familyId) {
    }

    public IssuedToken createAccessToken(UUID userId, String email) {
        Instant now = clock.instant();
        Instant expiresAt = now.plus(properties.accessTokenTtl());
        UUID id = UUID.randomUUID();
        String token = Jwts.builder()
                .id(id.toString())
                .issuer(properties.issuer())
                .subject(userId.toString())
                .claim("email", email)
                .claim(TYPE_CLAIM, "access")
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(accessKey, Jwts.SIG.HS256)
                .compact();
        return new IssuedToken(token, id, expiresAt);
    }

    public IssuedToken createRefreshToken(UUID userId, UUID familyId) {
        Instant now = clock.instant();
        Instant expiresAt = now.plus(properties.refreshTokenTtl());
        UUID id = UUID.randomUUID();
        String token = Jwts.builder()
                .id(id.toString())
                .issuer(properties.issuer())
                .subject(userId.toString())
                .claim(FAMILY_CLAIM, familyId.toString())
                .claim(TYPE_CLAIM, "refresh")
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(refreshKey, Jwts.SIG.HS256)
                .compact();
        return new IssuedToken(token, id, expiresAt);
    }

    /** @throws io.jsonwebtoken.ExpiredJwtException when expired; {@link JwtException} when otherwise invalid */
    public AuthenticatedUser parseAccessToken(String token) {
        Claims claims = parse(token, accessKey, "access");
        return new AuthenticatedUser(UUID.fromString(claims.getSubject()), claims.get("email", String.class));
    }

    public RefreshClaims parseRefreshToken(String token) {
        Claims claims = parse(token, refreshKey, "refresh");
        return new RefreshClaims(UUID.fromString(claims.getId()), UUID.fromString(claims.getSubject()),
                UUID.fromString(claims.get(FAMILY_CLAIM, String.class)));
    }

    public long accessTokenTtlSeconds() {
        return properties.accessTokenTtl().toSeconds();
    }

    private Claims parse(String token, SecretKey key, String expectedType) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .requireIssuer(properties.issuer())
                .clock(() -> Date.from(clock.instant()))
                .clockSkewSeconds(30)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        if (!expectedType.equals(claims.get(TYPE_CLAIM, String.class))) {
            throw new JwtException("Unexpected token type");
        }
        return claims;
    }

    private static SecretKey key(String secret, String name) {
        if (secret == null || secret.isBlank()) {
            byte[] random = new byte[64];
            new SecureRandom().nextBytes(random);
            log.warn("{} is not set. Using a random ephemeral key: all sessions end when the server restarts. "
                    + "Set {} (at least {} bytes) in production.", name, name, MIN_SECRET_BYTES);
            return Keys.hmacShaKeyFor(random);
        }
        byte[] bytes = decode(secret);
        if (bytes.length < MIN_SECRET_BYTES) {
            throw new IllegalStateException(name + " must be at least " + MIN_SECRET_BYTES + " bytes long");
        }
        return Keys.hmacShaKeyFor(bytes);
    }

    /** Accepts "base64:..." encoded secrets or raw strings. */
    private static byte[] decode(String secret) {
        if (secret.startsWith("base64:")) {
            return Base64.getDecoder().decode(secret.substring("base64:".length()));
        }
        return secret.getBytes(StandardCharsets.UTF_8);
    }
}
