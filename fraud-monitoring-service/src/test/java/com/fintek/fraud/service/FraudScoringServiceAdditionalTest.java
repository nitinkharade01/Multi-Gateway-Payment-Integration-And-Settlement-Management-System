package com.fintek.fraud.service;

import com.fintek.fraud.enums.*;
import com.fintek.fraud.mapper.FraudAlertMapper;
import com.fintek.fraud.repository.*;
import com.fintek.fraud.service.impl.FraudEventPublisher;
import com.fintek.fraud.service.impl.FraudScoringServiceImpl;
import com.fintek.fraud.support.TestDataFactory;
import com.fintek.fraud.validator.FraudRequestValidator;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FraudScoringServiceAdditionalTest {
    @Mock
    private FraudEvidenceProvider evidence;
    @Mock
    private FraudAlertRepository alerts;
    @Mock
    private FraudSignalRepository signals;
    @Mock
    private FraudEventPublisher events;

    private FraudScoringServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new FraudScoringServiceImpl(evidence, alerts, signals, events, new FraudAlertMapper(),
                new FraudRequestValidator());
    }

    @Test
    void shouldCalculateLowMediumAndHighRiskScores() {
        when(alerts.save(any())).thenAnswer(answer -> answer.getArgument(0));

        var low = service.assess(TestDataFactory.request(new BigDecimal("100.00"), 0, 0));
        assertEquals(RiskLevel.LOW, low.riskLevel(), "Safe transaction should be low risk");
        assertNull(low.alertId(), "Low risk transactions should not create alerts");

        var medium = service.assess(TestDataFactory.request(new BigDecimal("100000.01"), 0, 0));
        assertEquals(RiskLevel.MEDIUM, medium.riskLevel(), "High-value-only transaction should be medium risk");

        when(evidence.merchantPaymentsAfter(eq("mrc_1"), any())).thenReturn(5L);
        when(evidence.merchantFailuresAfter(eq("mrc_1"), any())).thenReturn(3L);
        when(evidence.customerPaymentsAfter(eq("+919876543210"), any())).thenReturn(8L);
        var high = service.assess(TestDataFactory.request(new BigDecimal("100000.01"), 3, 4));

        assertEquals(RiskLevel.HIGH, high.riskLevel(), "Combined fraud signals should be high risk");
        assertEquals(100, high.score(), "Risk score should be capped at 100");
        assertTrue(high.rules().containsAll(List.of(FraudRule.HIGH_VALUE_TRANSACTION, FraudRule.SUSPICIOUS_FREQUENCY,
                FraudRule.MULTIPLE_FAILED_ATTEMPTS, FraudRule.REFUND_ABUSE, FraudRule.CUSTOMER_PATTERN_RISK,
                FraudRule.WEBHOOK_FAILURE_RISK)), "All triggered fraud rules should be reported");
        verify(events, atLeastOnce()).publish(any());
    }

    @Test
    void shouldMapScoreToRiskBands() {
        assertEquals(RiskLevel.LOW, service.riskLevel(40), "Scores 0-40 should be low risk");
        assertEquals(RiskLevel.MEDIUM, service.riskLevel(41), "Scores 41-70 should be medium risk");
        assertEquals(RiskLevel.HIGH, service.riskLevel(71), "Scores 71-100 should be high risk");
    }

    @Test
    void shouldReturnFraudAlertsByMerchant() {
        when(alerts.findByMerchantId(eq("mrc_1"), any())).thenReturn(new PageImpl<>(List.of(
                TestDataFactory.alert(RiskLevel.HIGH, 85))));

        var page = service.alerts("mrc_1", PageRequest.of(0, 10));

        assertEquals(1, page.getTotalElements(), "Alert page should include merchant alerts");
        assertEquals(RiskLevel.HIGH, page.getContent().getFirst().riskLevel(), "High risk alert should map to response");
    }
}
