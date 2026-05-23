package com.fintek.payment.service;

import com.fintek.payment.dto.request.CreatePaymentOrderRequest;
import com.fintek.payment.dto.request.PaymentStatusUpdateRequest;
import com.fintek.payment.dto.response.*;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PaymentOrderService {
    PaymentOrderResponse create(CreatePaymentOrderRequest request);
    PaymentOrderResponse getOrder(String orderId);
    PaymentOrderResponse pay(String orderId);
    TransactionStatusResponse status(String transactionId);
    TransactionStatusResponse updateStatus(String transactionId, PaymentStatusUpdateRequest request);
    Page<PaymentOrderResponse> merchantOrders(String merchantId, Pageable pageable);
    TransactionSnapshotResponse transaction(String transactionId);
    List<TransactionSnapshotResponse> successfulTransactions(String merchantId, Instant from, Instant to);
}
