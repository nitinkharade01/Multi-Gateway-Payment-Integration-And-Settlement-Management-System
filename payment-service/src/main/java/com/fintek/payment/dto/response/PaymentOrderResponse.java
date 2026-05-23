package com.fintek.payment.dto.response;

import com.fintek.payment.enums.PaymentMode;
import com.fintek.payment.enums.PaymentStatus;
import java.math.BigDecimal;
import java.time.Instant;

public record PaymentOrderResponse(String orderId, String transactionId, String merchantId, BigDecimal amount,
                                   String currency, PaymentMode paymentMode, PaymentStatus status, String gateway,
                                   String checkoutUrl, Instant expiresAt, boolean idempotentReplay) {
}
