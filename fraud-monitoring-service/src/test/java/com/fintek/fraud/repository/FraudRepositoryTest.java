package com.fintek.fraud.repository;

import com.fintek.fraud.enums.RiskLevel;
import com.fintek.fraud.support.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class FraudRepositoryTest {
    @Autowired
    private FraudAlertRepository alerts;

    @Test
    void shouldReturnFraudAlertsByMerchant() {
        alerts.saveAndFlush(TestDataFactory.alert(RiskLevel.HIGH, 85));

        assertEquals(1, alerts.findByMerchantId("mrc_1", PageRequest.of(0, 10)).getTotalElements(),
                "Merchant alert query should include saved fraud alert");
    }
}
