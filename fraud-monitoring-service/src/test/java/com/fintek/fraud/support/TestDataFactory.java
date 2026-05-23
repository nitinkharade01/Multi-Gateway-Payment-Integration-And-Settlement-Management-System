package com.fintek.fraud.support;

import com.fintek.fraud.dto.request.FraudAssessmentRequest;
import com.fintek.fraud.entity.FraudAlert;
import com.fintek.fraud.enums.RiskLevel;
import java.math.BigDecimal;
import java.time.Instant;

public final class TestDataFactory {
    private TestDataFactory() {
    }

    public static FraudAssessmentRequest request(BigDecimal amount, int refunds, int failedWebhooks) {
        return new FraudAssessmentRequest("mrc_1", "txn_1", "+919876543210", amount, "SUCCESS",
                refunds, failedWebhooks);
    }

    public static FraudAlert alert(RiskLevel level, int score) {
        FraudAlert alert = new FraudAlert();
        alert.setId("db_alert");
        alert.setAlertId("fra_1");
        alert.setMerchantId("mrc_1");
        alert.setTransactionId("txn_1");
        alert.setScore(score);
        alert.setRiskLevel(level);
        alert.setReasons("HIGH_VALUE_TRANSACTION");
        alert.setCreatedAt(Instant.parse("2026-05-01T10:15:30Z"));
        return alert;
    }
}
