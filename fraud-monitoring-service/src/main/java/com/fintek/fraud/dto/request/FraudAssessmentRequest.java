package com.fintek.fraud.dto.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record FraudAssessmentRequest(@NotBlank String merchantId, @NotBlank String transactionId,
                                     @NotBlank @Pattern(regexp = "^\\+?[1-9]\\d{9,14}$") String customerPhone,
                                     @NotNull @Positive BigDecimal amount, @NotBlank String status,
                                     @Min(0) int recentRefundRequests, @Min(0) int failedWebhookCallbacks) {
}
