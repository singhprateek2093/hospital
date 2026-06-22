package com.hospital.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

/**
 * Creates and verifies JWTs (JSON Web Tokens).
 *
 * A JWT is three base64 parts: header.payload.signature. We sign the payload with
 * an HMAC-SHA256 key derived from app.jwt.secret. Because only the server knows the
 * secret, a token whose signature verifies is proof the server issued it — so we can
 * trust the email/role inside it without a DB lookup on every request.
 */
@Service
public class JwtService {

    private final SecretKey key;
    private final long expirationMs;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-ms}") long expirationMs) {
        // HS256 requires a 256-bit (32-byte) key. We SHA-256 the configured secret so
        // ANY secret length works — including platform-generated values (Render's
        // generateValue) whose length we don't control.
        this.key = Keys.hmacShaKeyFor(sha256(secret));
        this.expirationMs = expirationMs;
    }

    private static byte[] sha256(String input) {
        try {
            return MessageDigest.getInstance("SHA-256")
                    .digest(input.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    /** Build a signed token whose subject is the user's email, with a role claim. */
    public String generateToken(String email, String role) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);
        return Jwts.builder()
                .subject(email)
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    /** Returns the email (subject) inside a valid token. Throws if invalid/expired. */
    public String extractEmail(String token) {
        return parse(token).getSubject();
    }

    /** Verifies signature + expiry; returns the claims payload. */
    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
