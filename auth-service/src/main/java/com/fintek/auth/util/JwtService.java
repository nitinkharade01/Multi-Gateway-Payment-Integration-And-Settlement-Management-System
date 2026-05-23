package com.fintek.auth.util;

import com.fintek.auth.entity.UserAccount;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtService {
    private final String issuer;
    private final Duration accessTtl;
    private final SecretKey secretKey;

    public JwtService(String issuer, String secret, long accessTtlMinutes) {
        this(issuer, secret, Duration.ofMinutes(accessTtlMinutes));
    }

    @Autowired
    public JwtService(@Value("${security.jwt.issuer}") String issuer,
                      @Value("${security.jwt.secret}") String secret,
                      @Value("${security.jwt.access-ttl-millis:${JWT_EXPIRATION:900000}}") Long accessTtlMillis) {
        this(issuer, secret, Duration.ofMillis(accessTtlMillis));
    }

    private JwtService(String issuer, String secret, Duration accessTtl) {
        this.issuer = issuer;
        this.accessTtl = accessTtl;
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public IssuedToken issue(UserAccount user) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(accessTtl);
        String value = Jwts.builder()
                .issuer(issuer)
                .subject(user.getEmail())
                .claim("uid", user.getId())
                .claim("role", user.getRole().name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(secretKey)
                .compact();
        return new IssuedToken(value, expiresAt);
    }

    public Claims parse(String token) {
        return Jwts.parser().verifyWith(secretKey).requireIssuer(issuer).build().parseSignedClaims(token).getPayload();
    }

    public record IssuedToken(String value, Instant expiresAt) {
    }
}
