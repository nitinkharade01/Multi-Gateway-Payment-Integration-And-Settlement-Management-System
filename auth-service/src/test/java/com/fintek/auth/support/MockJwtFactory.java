package com.fintek.auth.support;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

public final class MockJwtFactory {
    private MockJwtFactory() {
    }

    public static Claims merchantClaims(String email) {
        return Jwts.claims().subject(email).add("role", "MERCHANT").build();
    }
}
