package com.fintek.payment.util;

import java.security.SecureRandom;
import java.util.HexFormat;

public final class PaymentIds {
    private static final SecureRandom RANDOM = new SecureRandom();

    private PaymentIds() {
    }

    public static String orderId() {
        return "ord_" + random();
    }

    public static String transactionId() {
        return "txn_" + random();
    }

    private static String random() {
        byte[] bytes = new byte[16];
        RANDOM.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }
}
