package com.fintek.merchant.dto.response;

import java.math.BigDecimal;

public record CredentialValidationResponse(boolean valid, String merchantId, String merchantStatus,
                                           String webhookUrl, BigDecimal singlePaymentLimit, String reason) {
}
