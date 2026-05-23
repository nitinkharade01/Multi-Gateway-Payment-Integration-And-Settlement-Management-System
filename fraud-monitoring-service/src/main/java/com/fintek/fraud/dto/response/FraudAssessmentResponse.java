package com.fintek.fraud.dto.response;

import com.fintek.fraud.enums.FraudRule;
import com.fintek.fraud.enums.RiskLevel;
import java.util.Set;

public record FraudAssessmentResponse(String merchantId, String transactionId, int score, RiskLevel riskLevel,
                                      Set<FraudRule> rules, String alertId) {
}
