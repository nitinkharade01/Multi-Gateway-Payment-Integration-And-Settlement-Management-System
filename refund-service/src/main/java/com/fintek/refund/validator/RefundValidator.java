package com.fintek.refund.validator;

import com.fintek.refund.dto.request.CreateRefundRequest;
import com.fintek.refund.dto.response.PaymentTransactionSnapshot;
import com.fintek.refund.enums.PaymentStatus;
import com.fintek.refund.exception.RefundException;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class RefundValidator {
    public void validate(CreateRefundRequest request, PaymentTransactionSnapshot paid, BigDecimal refundable) {
        if (!request.merchantId().equals(paid.merchantId())) {
            throw new RefundException(403, "Transaction does not belong to refund merchant");
        }
        if (paid.status() != PaymentStatus.SUCCESS && paid.status() != PaymentStatus.PARTIALLY_REFUNDED) {
            throw new RefundException(409, "Only successful paid transactions can be refunded");
        }
        if (request.amount().compareTo(refundable) > 0) {
            throw new RefundException(422, "Refund amount exceeds available refundable amount " + refundable);
        }
    }
}
