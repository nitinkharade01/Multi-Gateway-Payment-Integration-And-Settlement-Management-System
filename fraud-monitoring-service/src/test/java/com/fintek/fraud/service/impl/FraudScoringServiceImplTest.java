package com.fintek.fraud.service.impl;

import com.fintek.fraud.dto.request.FraudAssessmentRequest;
import com.fintek.fraud.dto.response.FraudAssessmentResponse;
import com.fintek.fraud.entity.FraudAlert;
import com.fintek.fraud.enums.*;
import com.fintek.fraud.mapper.FraudAlertMapper;
import com.fintek.fraud.repository.*;
import com.fintek.fraud.service.FraudEvidenceProvider;
import com.fintek.fraud.validator.FraudRequestValidator;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class FraudScoringServiceImplTest {
    private FraudEvidenceProvider evidence;
    private FraudScoringServiceImpl fraud;

    @BeforeEach
    void setUp() {
        evidence = mock(FraudEvidenceProvider.class);
        FraudAlertRepository alerts = mock(FraudAlertRepository.class);
        when(alerts.save(any())).thenAnswer(answer -> answer.getArgument(0));
        fraud = new FraudScoringServiceImpl(evidence, alerts, mock(FraudSignalRepository.class),
                mock(FraudEventPublisher.class), new FraudAlertMapper(), new FraudRequestValidator());
    }

    @Test
    void fraudHighValueTransactionDetectionAddsRule() {
        FraudAssessmentResponse response = fraud.assess(request("100000.01"));

        assertTrue(response.rules().contains(FraudRule.HIGH_VALUE_TRANSACTION));
        assertEquals(RiskLevel.MEDIUM, response.riskLevel());
    }

    @Test
    void fraudSuspiciousFrequencyDetectionAddsRule() {
        when(evidence.merchantPaymentsAfter(eq("mrc_1"), any())).thenReturn(5L);

        FraudAssessmentResponse response = fraud.assess(request("100.00"));

        assertTrue(response.rules().contains(FraudRule.SUSPICIOUS_FREQUENCY));
        assertEquals(30, response.score());
    }

    private FraudAssessmentRequest request(String amount) {
        return new FraudAssessmentRequest("mrc_1", "txn_1", "+919876543210", new BigDecimal(amount),
                "SUCCESS", 0, 0);
    }
}
