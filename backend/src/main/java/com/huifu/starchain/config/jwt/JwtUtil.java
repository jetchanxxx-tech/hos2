package com.huifu.starchain.config.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;


@Component
public class JwtUtil {

    private final SecretKey key;
    private final long accessExpiration;
    private final long refreshExpiration;
    private final String issuer;

    public JwtUtil(SecretKey key, long accessExpiration, long refreshExpiration, String issuer) { this.key = key; this.accessExpiration = accessExpiration; this.refreshExpiration = refreshExpiration; this.issuer = issuer; }

    public JwtUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-expiration}") long accessExpiration,
            @Value("${jwt.refresh-token-expiration}") long refreshExpiration,
            @Value("${jwt.issuer}") String issuer) {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(encodeBase64(secret)));
        this.accessExpiration = accessExpiration;
        this.refreshExpiration = refreshExpiration;
        this.issuer = issuer;
    }

    private String encodeBase64(String raw) {
        byte[] bytes = raw.getBytes();
        byte[] base64 = java.util.Base64.getEncoder().encode(bytes);
        // Pad to 256 bits minimum for HMAC-SHA256
        if (base64.length < 43) {
            byte[] padded = new byte[64];
            System.arraycopy(base64, 0, padded, 0, base64.length);
            return new String(padded);
        }
        return new String(base64);
    }

    public String generateAccessToken(Long userId, String role) {
        return buildToken(userId, role, accessExpiration);
    }

    public String generateRefreshToken(Long userId, String role) {
        return buildToken(userId, role, refreshExpiration);
    }

    private String buildToken(Long userId, String role, long expiration) {
        Date now = new Date();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("role", role)
                .issuer(issuer)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expiration * 1000))
                .signWith(key)
                .compact();
    }

    public Jws<Claims> parseToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .requireIssuer(issuer)
                .build()
                .parseSignedClaims(token);
    }

    public Long getUserId(String token) {
        return Long.valueOf(parseToken(token).getPayload().getSubject());
    }

    public String getRole(String token) {
        return parseToken(token).getPayload().get("role", String.class);
    }

    public boolean isTokenValid(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("JWT validation failed: {}", e.getMessage());
            return false;
        }
    }
}
