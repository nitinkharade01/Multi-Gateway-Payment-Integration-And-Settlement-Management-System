package com.fintek.fraud.dto.response;

import com.fintek.fraud.enums.RiskLevel;
import java.time.Instant;

public record FraudAlertResponse(String alertId, String merchantId, String transactionId, int score,
                                 RiskLevel riskLevel, String reasons, Instant createdAt) {
}
