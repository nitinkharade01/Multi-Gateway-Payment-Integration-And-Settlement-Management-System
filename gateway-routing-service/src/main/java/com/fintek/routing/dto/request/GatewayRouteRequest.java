package com.fintek.routing.dto.request;

import com.fintek.routing.enums.PaymentMode;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record GatewayRouteRequest(@NotBlank String merchantId, @NotBlank String orderId, @NotBlank String transactionId,
                                  @NotNull @Positive BigDecimal amount, @NotBlank String currency,
                                  @NotNull PaymentMode paymentMode) {
}
