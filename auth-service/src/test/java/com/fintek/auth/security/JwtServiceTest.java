package com.fintek.auth.security;

import com.fintek.auth.support.TestDataFactory;
import com.fintek.auth.util.JwtService;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {
    private static final String SECRET = "test-secret-with-enough-length-for-hmac-sha-signing-512-bits";

    @Test
    void shouldGenerateJwtToken() {
        JwtService jwtService = new JwtService("payment-platform-test", SECRET, 15);

        var token = jwtService.issue(TestDataFactory.activeUser());

        assertNotNull(token.value(), "JWT value should be generated");
        assertTrue(token.expiresAt().isAfter(java.time.Instant.now()), "JWT expiration should be in the future");
    }

    @Test
    void shouldValidateJwtTokenAndExtractUsername() {
        JwtService jwtService = new JwtService("payment-platform-test", SECRET, 15);
        String token = jwtService.issue(TestDataFactory.activeUser()).value();

        var claims = jwtService.parse(token);

        assertEquals(TestDataFactory.EMAIL, claims.getSubject(), "JWT subject should contain the user email");
        assertEquals("MERCHANT", claims.get("role", String.class), "JWT role claim should be preserved");
    }

    @Test
    void shouldRejectExpiredJwtToken() {
        JwtService expiredJwtService = new JwtService("payment-platform-test", SECRET, -1);
        String token = expiredJwtService.issue(TestDataFactory.activeUser()).value();

        assertThrows(ExpiredJwtException.class, () -> expiredJwtService.parse(token),
                "Expired JWTs must be rejected");
    }
}
