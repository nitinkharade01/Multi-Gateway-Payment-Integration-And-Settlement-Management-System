package com.fintek.reconciliation.dto.response;

import java.math.BigDecimal;
import java.time.Instant;

public record InternalTransactionRecord(String transactionId, String orderId, String merchantId, BigDecimal amount,
                                        String currency, String status, String gateway, Instant createdAt) {
}
