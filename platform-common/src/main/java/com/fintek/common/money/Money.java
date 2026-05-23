package com.fintek.common.money;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class Money {
    public static final int SCALE = 2;

    private Money() {
    }

    public static BigDecimal scale(BigDecimal value) {
        return require(value).setScale(SCALE, RoundingMode.HALF_UP);
    }

    public static BigDecimal percentage(BigDecimal amount, BigDecimal percentage) {
        return scale(require(amount).multiply(require(percentage)).divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP));
    }

    public static BigDecimal require(BigDecimal value) {
        if (value == null) {
            throw new IllegalArgumentException("Money value is required");
        }
        return value;
    }
}
