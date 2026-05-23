package com.fintek.settlement.dto.response;

import java.math.BigDecimal;
import java.time.Instant;

public record SettlementTransactionSnapshot(String transactionId, String orderId, String merchantId, BigDecimal amount,
                                            String currency, String status, String gateway, Instant createdAt) {
}
