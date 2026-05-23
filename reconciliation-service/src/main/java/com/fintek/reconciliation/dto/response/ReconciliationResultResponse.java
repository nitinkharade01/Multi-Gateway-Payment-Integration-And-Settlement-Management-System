package com.fintek.reconciliation.dto.response;

import com.fintek.reconciliation.enums.ReconciliationStatus;
import java.math.BigDecimal;

public record ReconciliationResultResponse(String runId, String transactionId, BigDecimal internalAmount,
                                           BigDecimal gatewayAmount, String internalStatus, String gatewayStatus,
                                           ReconciliationStatus status, String reason) {
}
