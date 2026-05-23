package com.fintek.payment.repository;

import com.fintek.payment.enums.PaymentStatus;
import com.fintek.payment.support.PaymentTestDataBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class PaymentRepositoryTest {
    @Autowired
    private PaymentOrderRepository orders;
    @Autowired
    private TransactionRepository transactions;

    @Test
    void shouldFindPaymentOrderByOrderIdAndIdempotencyKey() {
        var order = orders.saveAndFlush(PaymentTestDataBuilder.order("ord_1", "idem-1", PaymentStatus.PENDING));

        assertTrue(orders.findByOrderId("ord_1").isPresent(), "Order ID lookup should find persisted order");
        assertTrue(orders.findByMerchantIdAndIdempotencyKey(order.getMerchantId(), "idem-1").isPresent(),
                "Merchant and idempotency key lookup should find persisted order");
    }

    @Test
    void shouldEnforceUniqueMerchantIdAndIdempotencyKey() {
        orders.saveAndFlush(PaymentTestDataBuilder.order("ord_1", "idem-1", PaymentStatus.PENDING));

        assertThrows(DataIntegrityViolationException.class,
                () -> orders.saveAndFlush(PaymentTestDataBuilder.order("ord_2", "idem-1", PaymentStatus.PENDING)),
                "A merchant should not be able to reuse an idempotency key for two orders");
    }

    @Test
    void shouldFindTransactionByTransactionId() {
        var order = orders.saveAndFlush(PaymentTestDataBuilder.order("ord_1", "idem-1", PaymentStatus.SUCCESS));
        transactions.saveAndFlush(PaymentTestDataBuilder.transaction(order, "txn_1", PaymentStatus.SUCCESS));

        assertTrue(transactions.findByTransactionId("txn_1").isPresent(),
                "Transaction ID lookup should find persisted transaction");
        assertEquals(1, transactions.findByMerchantIdAndCreatedAtBetweenAndStatus(order.getMerchantId(),
                order.getCreatedAt().minusSeconds(1), order.getCreatedAt().plusSeconds(1), PaymentStatus.SUCCESS).size(),
                "Successful transaction range lookup should include matching transaction");
    }
}
