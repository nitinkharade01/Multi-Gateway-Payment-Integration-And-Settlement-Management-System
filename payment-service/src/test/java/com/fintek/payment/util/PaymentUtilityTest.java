package com.fintek.payment.util;

import com.fintek.payment.support.PaymentTestDataBuilder;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PaymentUtilityTest {
    @Test
    void shouldGenerateUniqueOrderIdAndTransactionId() {
        assertNotEquals(PaymentIds.orderId(), PaymentIds.orderId(), "Order IDs should be unique");
        assertNotEquals(PaymentIds.transactionId(), PaymentIds.transactionId(), "Transaction IDs should be unique");
        assertTrue(PaymentIds.orderId().startsWith("ord_"), "Order IDs should use ord_ prefix");
        assertTrue(PaymentIds.transactionId().startsWith("txn_"), "Transaction IDs should use txn_ prefix");
    }

    @Test
    void shouldCreateStableAndPayloadSensitiveFingerprint() {
        var first = PaymentTestDataBuilder.createOrderRequest("idem-1", new BigDecimal("100.00"));
        var same = PaymentTestDataBuilder.createOrderRequest("idem-1", new BigDecimal("100.00"));
        var changed = PaymentTestDataBuilder.createOrderRequest("idem-1", new BigDecimal("101.00"));

        assertEquals(PaymentFingerprints.from(first, "INR"), PaymentFingerprints.from(same, "INR"),
                "Same payload should produce same idempotency fingerprint");
        assertNotEquals(PaymentFingerprints.from(first, "INR"), PaymentFingerprints.from(changed, "INR"),
                "Changed payload should produce a different idempotency fingerprint");
    }
}
