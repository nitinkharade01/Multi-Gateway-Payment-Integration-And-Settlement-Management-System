package com.fintek.common.support;

import java.math.BigDecimal;
import java.time.Instant;

public final class TestDataFactory {
    public static final Instant FIXED_TIME = Instant.parse("2026-05-01T10:15:30Z");
    public static final BigDecimal PAYMENT_AMOUNT = new BigDecimal("1250.50");

    private TestDataFactory() {
    }
}
