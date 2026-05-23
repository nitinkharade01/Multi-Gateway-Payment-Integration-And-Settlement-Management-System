package com.fintek.payment.dto.request;

import com.fintek.payment.enums.PaymentMode;
import java.math.BigDecimal;

public record GatewayRouteRequest(String merchantId, String orderId, String transactionId, BigDecimal amount,
                                  String currency, PaymentMode paymentMode) {
}
