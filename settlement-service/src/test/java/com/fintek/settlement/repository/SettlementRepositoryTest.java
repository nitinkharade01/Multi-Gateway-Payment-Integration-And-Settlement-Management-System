package com.fintek.settlement.repository;

import com.fintek.settlement.support.SettlementTestDataBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class SettlementRepositoryTest {
    @Autowired
    private SettlementRepository settlements;

    @Test
    void shouldFindSettlementBySettlementIdAndMerchant() {
        var settlement = settlements.saveAndFlush(SettlementTestDataBuilder.settlement());

        assertTrue(settlements.findBySettlementId("set_1").isPresent(),
                "Settlement ID lookup should find saved settlement");
        assertEquals(1, settlements.findByMerchantId("mrc_1", PageRequest.of(0, 10)).getTotalElements(),
                "Merchant settlement lookup should include saved settlement");
        assertTrue(settlements.existsByMerchantIdAndRangeStartAndRangeEnd("mrc_1",
                settlement.getRangeStart(), settlement.getRangeEnd()),
                "Duplicate settlement range detection should find saved settlement");
    }
}
