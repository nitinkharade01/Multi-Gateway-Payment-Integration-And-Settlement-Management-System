package com.fintek.common.events;

import java.math.BigDecimal;
import java.time.Instant;

public record RefundSuccessEvent(String eventId, Instant occurredAt, String merchantId, String refundId,
                                 String transactionId, BigDecimal amount) implements PaymentEvent {
}
