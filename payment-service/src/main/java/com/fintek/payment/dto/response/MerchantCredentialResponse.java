package com.fintek.payment.dto.response;

import java.math.BigDecimal;

public record MerchantCredentialResponse(boolean valid, String merchantId, String merchantStatus,
                                         String webhookUrl, BigDecimal singlePaymentLimit, String reason) {
}
