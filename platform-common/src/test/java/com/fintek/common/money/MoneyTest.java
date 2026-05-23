package com.fintek.common.money;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MoneyTest {
    @Test
    void shouldScaleMoneyUsingHalfUp() {
        assertEquals(new BigDecimal("10.24"), Money.scale(new BigDecimal("10.235")),
                "Money should round to scale two using HALF_UP");
    }

    @Test
    void shouldCalculatePercentageAtScaleTwo() {
        assertEquals(new BigDecimal("18.00"), Money.percentage(new BigDecimal("100.00"), new BigDecimal("18.00")),
                "Percentage calculation should return scale two");
    }

    @Test
    void shouldRejectNullMoneyValues() {
        assertThrows(IllegalArgumentException.class, () -> Money.scale(null),
                "Null money input should be rejected immediately");
    }
}
