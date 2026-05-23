package com.fintek.common.events;

import com.fintek.common.support.TestDataFactory;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PaymentEventTest {
    @Test
    void shouldCreatePaymentEventRecord() {
        PaymentEvent event = new PaymentCreatedEvent("evt_1", TestDataFactory.FIXED_TIME, "mrc_1",
                "ord_1", "txn_1", TestDataFactory.PAYMENT_AMOUNT, "INR");

        assertEquals("evt_1", event.eventId(), "Event ID should be preserved");
        assertEquals(TestDataFactory.FIXED_TIME, event.occurredAt(), "Occurrence timestamp should be preserved");
        assertInstanceOf(PaymentCreatedEvent.class, event, "Created event should implement PaymentEvent");
    }

    @Test
    void shouldPreserveFailureReasonAndWebhookUrl() {
        var event = new PaymentFailedEvent("evt_2", TestDataFactory.FIXED_TIME, "mrc_1", "ord_1",
                "txn_1", new BigDecimal("10.00"), "INR", "gateway declined", "https://merchant.test/hook");

        assertEquals("gateway declined", event.reason(), "Failure reason should remain intact");
        assertEquals("https://merchant.test/hook", event.merchantWebhookUrl(), "Merchant callback URL should remain intact");
    }

    @Test
    void shouldCreateSettlementAndFraudEvents() {
        var settlement = new SettlementGeneratedEvent("evt_3", TestDataFactory.FIXED_TIME, "mrc_1",
                "set_1", new BigDecimal("968.20"));
        var fraud = new FraudAlertCreatedEvent("evt_4", TestDataFactory.FIXED_TIME, "mrc_1",
                "txn_1", 85, "HIGH", "HIGH_VALUE_TRANSACTION");

        assertEquals(new BigDecimal("968.20"), settlement.netAmount(), "Settlement net amount should be preserved");
        assertEquals(85, fraud.score(), "Fraud score should be preserved");
        assertEquals("HIGH", fraud.riskLevel(), "Fraud risk level should be preserved");
    }
}
