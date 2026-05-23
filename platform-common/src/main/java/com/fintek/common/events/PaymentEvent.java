package com.fintek.common.events;

import java.time.Instant;

public sealed interface PaymentEvent permits PaymentCreatedEvent, PaymentSuccessEvent, PaymentFailedEvent,
        RefundSuccessEvent, SettlementGeneratedEvent, FraudAlertCreatedEvent {

    String eventId();

    Instant occurredAt();
}
