package com.fintek.routing.repository;

import com.fintek.routing.enums.GatewayHealth;
import com.fintek.routing.enums.GatewayName;
import com.fintek.routing.support.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class GatewayConfigRepositoryTest {
    @Autowired
    private GatewayConfigRepository configs;

    @Test
    void shouldFindGatewayConfigByGatewayName() {
        configs.saveAndFlush(TestDataFactory.config(GatewayName.CASHFREE_SIMULATOR, GatewayHealth.ACTIVE, 1));

        assertTrue(configs.findByGateway(GatewayName.CASHFREE_SIMULATOR).isPresent(),
                "Gateway config should be findable by gateway enum");
        assertTrue(configs.findByGateway(GatewayName.PAYU_SIMULATOR).isEmpty(),
                "Unknown gateway config should return empty");
    }
}
