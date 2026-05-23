package com.fintek.merchant.dto.response;

import com.fintek.merchant.enums.KycStatus;
import com.fintek.merchant.enums.MerchantStatus;
import java.math.BigDecimal;
import java.time.Instant;

public record MerchantResponse(String merchantId, String businessName, String email, String phone,
                               MerchantStatus status, KycStatus kycStatus, String webhookUrl,
                               BigDecimal singlePaymentLimit, Instant createdAt) {
}
