package com.fintek.merchant.util;

import java.security.SecureRandom;
import java.util.Base64;

public final class ApiCredentials {
    private static final SecureRandom RANDOM = new SecureRandom();

    private ApiCredentials() {
    }

    public static String apiKey() {
        return "pk_live_" + random(24);
    }

    public static String apiSecret() {
        return "sk_live_" + random(42);
    }

    private static String random(int size) {
        byte[] bytes = new byte[size];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
