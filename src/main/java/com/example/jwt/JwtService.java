package com.example.jwt;

import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {
    private static final Set<String> RESERVED = Set.of("iss", "sub", "aud", "exp", "nbf", "iat", "jti");
    private final SecretKey key;
    private final long expirationSeconds;

    public JwtService(@Value("${jwt.secret-base64:}") String secret,
                      @Value("${jwt.expiration-seconds:3600}") long expirationSeconds) {
        this.key = secret.isBlank() ? Jwts.SIG.HS256.key().build()
                : Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        if (expirationSeconds <= 0) throw new IllegalArgumentException("Expiration must be positive");
        this.expirationSeconds = expirationSeconds;
    }

    public TokenResponse generate(Map<String, Object> user) {
        if (user == null || !(user.get("username") instanceof String username) || username.isBlank()) {
            throw new IllegalArgumentException("username must be a non-empty string");
        }
        if (user.keySet().stream().anyMatch(RESERVED::contains)) {
            throw new IllegalArgumentException("Do not supply reserved JWT claims: " + RESERVED);
        }
        Instant issued = Instant.now();
        Instant expires = issued.plusSeconds(expirationSeconds);
        String token = Jwts.builder().claims(user).subject((String) user.get("username"))
                .issuer("jwt-generator").issuedAt(Date.from(issued)).expiration(Date.from(expires))
                .signWith(key, Jwts.SIG.HS256).compact();
        return new TokenResponse(token, "Bearer", expirationSeconds, expires);
    }

    public Map<String, Object> verify(String token) {
        return Jwts.parser().verifyWith(key).requireIssuer("jwt-generator")
                .build().parseSignedClaims(token).getPayload();
    }

    public record TokenResponse(String token, String tokenType, long expiresIn, Instant expiresAt) {}
}
