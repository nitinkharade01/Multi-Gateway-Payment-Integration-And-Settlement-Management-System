package com.fintek.notification.support;

import com.fintek.common.events.*;
import com.fintek.notification.entity.NotificationLog;
import com.fintek.notification.enums.*;
import java.math.BigDecimal;
import java.time.Instant;

public final class TestDataFactory {
    public static final Instant TIME = Instant.parse("2026-05-01T10:15:30Z");

    private TestDataFactory() {
    }

    public static PaymentCreatedEvent paymentCreated() {
        return new PaymentCreatedEvent("evt_created", TIME, "mrc_1", "ord_1", "txn_1",
                new BigDecimal("100.00"), "INR");
    }

    public static PaymentSuccessEvent paymentSuccess() {
        return new PaymentSuccessEvent("evt_success", TIME, "mrc_1", "ord_1", "txn_1",
                new BigDecimal("100.00"), "INR", "https://merchant.test/hook");
    }

    public static PaymentFailedEvent paymentFailed() {
        return new PaymentFailedEvent("evt_failed", TIME, "mrc_1", "ord_1", "txn_1",
                new BigDecimal("100.00"), "INR", "declined", "https://merchant.test/hook");
    }

    public static RefundSuccessEvent refundSuccess() {
        return new RefundSuccessEvent("evt_refund", TIME, "mrc_1", "rfnd_1", "txn_1", new BigDecimal("25.00"));
    }

    public static SettlementGeneratedEvent settlementGenerated() {
        return new SettlementGeneratedEvent("evt_settlement", TIME, "mrc_1", "set_1", new BigDecimal("96.82"));
    }

    public static FraudAlertCreatedEvent fraudAlert() {
        return new FraudAlertCreatedEvent("evt_fraud", TIME, "mrc_1", "txn_1", 85, "HIGH",
                "HIGH_VALUE_TRANSACTION");
    }

    public static NotificationLog log() {
        NotificationLog log = new NotificationLog();
        log.setId("log_1");
        log.setEventId("evt_success");
        log.setMerchantId("mrc_1");
        log.setChannel(NotificationChannel.EMAIL);
        log.setStatus(NotificationStatus.SIMULATED);
        log.setDestination("merchant-contact:mrc_1");
        log.setRenderedMessage("message");
        log.setCreatedAt(TIME);
        return log;
    }
}
