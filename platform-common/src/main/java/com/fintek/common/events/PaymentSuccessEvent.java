package com.fintek.common.events;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentSuccessEvent(String eventId, Instant occurredAt, String merchantId, String orderId,
                                  String transactionId, BigDecimal amount, String currency,
                                  String merchantWebhookUrl) implements PaymentEvent {
}
