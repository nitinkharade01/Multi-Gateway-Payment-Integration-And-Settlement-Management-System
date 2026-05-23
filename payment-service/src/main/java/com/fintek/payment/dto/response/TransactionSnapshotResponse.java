package com.fintek.payment.dto.response;

import com.fintek.payment.enums.PaymentStatus;
import java.math.BigDecimal;
import java.time.Instant;

public record TransactionSnapshotResponse(String transactionId, String orderId, String merchantId, BigDecimal amount,
                                          String currency, PaymentStatus status, String gateway, Instant createdAt) {
}
