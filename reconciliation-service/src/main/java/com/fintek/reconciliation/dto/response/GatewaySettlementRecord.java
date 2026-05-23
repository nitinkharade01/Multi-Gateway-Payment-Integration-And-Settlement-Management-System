package com.fintek.reconciliation.dto.response;

import java.math.BigDecimal;

public record GatewaySettlementRecord(String transactionId, BigDecimal amount, String status, String gatewayReference) {
}
