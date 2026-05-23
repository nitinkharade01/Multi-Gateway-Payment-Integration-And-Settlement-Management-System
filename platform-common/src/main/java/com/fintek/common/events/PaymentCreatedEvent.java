package com.fintek.common.events;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentCreatedEvent(String eventId, Instant occurredAt, String merchantId, String orderId,
                                  String transactionId, BigDecimal amount, String currency) implements PaymentEvent {
}
