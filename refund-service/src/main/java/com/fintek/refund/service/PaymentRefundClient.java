package com.fintek.refund.service;

import com.fintek.refund.dto.response.PaymentTransactionSnapshot;
import com.fintek.refund.enums.PaymentStatus;

public interface PaymentRefundClient {
    PaymentTransactionSnapshot transaction(String transactionId);
    void update(String transactionId, PaymentStatus status);
}
