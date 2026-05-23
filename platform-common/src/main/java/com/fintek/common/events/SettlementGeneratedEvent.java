package com.fintek.common.events;

import java.math.BigDecimal;
import java.time.Instant;

public record SettlementGeneratedEvent(String eventId, Instant occurredAt, String merchantId, String settlementId,
                                       BigDecimal netAmount) implements PaymentEvent {
}
