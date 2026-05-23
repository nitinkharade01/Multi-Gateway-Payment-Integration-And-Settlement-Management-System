package com.fintek.refund.util;

import java.util.UUID;

public final class RefundIds {
    private RefundIds() {
    }

    public static String refundId() {
        return "rfnd_" + UUID.randomUUID().toString().replace("-", "");
    }
}
