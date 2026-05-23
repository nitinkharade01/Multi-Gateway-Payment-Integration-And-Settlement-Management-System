package com.fintek.auth.dto.response;

import java.time.Instant;

public record AuthResponse(String accessToken, String refreshToken, Instant accessTokenExpiresAt,
                           UserProfileResponse profile) {
}
