package com.fintek.payment.dto.request;

import com.fintek.payment.enums.PaymentMode;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record CreatePaymentOrderRequest(
        @NotBlank String merchantId,
        @NotBlank String apiKey,
        @NotBlank String apiSecret,
        @NotBlank @Size(max = 160) String idempotencyKey,
        @NotNull @Positive @Digits(integer = 16, fraction = 2) BigDecimal amount,
        @Pattern(regexp = "^[A-Z]{3}$") String currency,
        @NotNull PaymentMode paymentMode,
        @NotBlank @Email String customerEmail,
        @NotBlank @Pattern(regexp = "^\\+?[1-9]\\d{9,14}$") String customerPhone,
        @Size(max = 500) String returnUrl) {
}
