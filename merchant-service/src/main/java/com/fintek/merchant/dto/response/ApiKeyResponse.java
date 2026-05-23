package com.fintek.merchant.dto.response;

import java.time.Instant;

public record ApiKeyResponse(String merchantId, String apiKey, String apiSecret, Instant expiresAt,
                             String secretNotice) {
}
