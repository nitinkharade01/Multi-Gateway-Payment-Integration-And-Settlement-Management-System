package com.fintek.refund.dto.response;

import com.fintek.refund.enums.RefundStatus;
import java.math.BigDecimal;
import java.time.Instant;

public record RefundResponse(String refundId, String merchantId, String transactionId, BigDecimal amount,
                             BigDecimal remainingRefundableAmount, RefundStatus status, String gatewayReference,
                             Instant createdAt) {
}
