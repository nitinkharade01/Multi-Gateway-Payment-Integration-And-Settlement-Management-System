package com.fintek.payment.util;

import com.fintek.payment.dto.request.CreatePaymentOrderRequest;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public final class PaymentFingerprints {
    private PaymentFingerprints() {
    }

    public static String from(CreatePaymentOrderRequest request, String currency) {
        String canonical = String.join("|", request.merchantId(), request.amount().stripTrailingZeros().toPlainString(),
                currency, request.paymentMode().name(), request.customerEmail().trim().toLowerCase(),
                request.customerPhone().trim(), request.returnUrl() == null ? "" : request.returnUrl().trim());
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(canonical.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException error) {
            throw new IllegalStateException("SHA-256 is unavailable", error);
        }
    }
}
