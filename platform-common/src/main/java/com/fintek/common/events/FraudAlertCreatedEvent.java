package com.fintek.common.events;

import java.time.Instant;

public record FraudAlertCreatedEvent(String eventId, Instant occurredAt, String merchantId, String transactionId,
                                     int score, String riskLevel, String reasons) implements PaymentEvent {
}
