package com.fintek.refund.dto.response;

import com.fintek.refund.enums.PaymentStatus;
import java.math.BigDecimal;
import java.time.Instant;

public record PaymentTransactionSnapshot(String transactionId, String orderId, String merchantId, BigDecimal amount,
                                         String currency, PaymentStatus status, String gateway, Instant createdAt) {
}
