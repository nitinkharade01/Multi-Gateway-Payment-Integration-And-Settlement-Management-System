package com.fintek.refund.repository;

import com.fintek.refund.enums.RefundStatus;
import com.fintek.refund.support.RefundTestDataBuilder;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class RefundRepositoryTest {
    @Autowired
    private RefundRepository refunds;

    @Test
    void shouldFindRefundByRefundIdAndMerchantId() {
        refunds.saveAndFlush(RefundTestDataBuilder.refund("rfnd_1", new BigDecimal("10.00"), RefundStatus.REFUND_SUCCESS));

        assertTrue(refunds.findByRefundId("rfnd_1").isPresent(), "Refund ID lookup should find saved refund");
        assertEquals(1, refunds.findByMerchantId("mrc_1", PageRequest.of(0, 10)).getTotalElements(),
                "Merchant refund lookup should include saved refund");
    }

    @Test
    void shouldFindSuccessfulRefundsForTransaction() {
        refunds.saveAndFlush(RefundTestDataBuilder.refund("rfnd_1", new BigDecimal("10.00"), RefundStatus.REFUND_SUCCESS));
        refunds.saveAndFlush(RefundTestDataBuilder.refund("rfnd_2", new BigDecimal("5.00"), RefundStatus.REFUND_FAILED));

        var successful = refunds.findByTransactionIdAndStatusIn("txn_1", List.of(RefundStatus.REFUND_SUCCESS));

        assertEquals(1, successful.size(), "Only successful refunds should be returned for refundable amount calculation");
    }
}
