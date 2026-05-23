package com.fintek.payment.controller;

import com.fintek.payment.dto.request.CreatePaymentOrderRequest;
import com.fintek.payment.dto.request.PaymentStatusUpdateRequest;
import com.fintek.payment.dto.response.*;
import com.fintek.payment.service.PaymentOrderService;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    private final PaymentOrderService payments;

    public PaymentController(PaymentOrderService payments) {
        this.payments = payments;
    }

    @PostMapping("/orders")
    @ResponseStatus(HttpStatus.CREATED)
    PaymentOrderResponse create(@Valid @RequestBody CreatePaymentOrderRequest request) {
        return payments.create(request);
    }

    @GetMapping("/orders/{orderId}")
    PaymentOrderResponse order(@PathVariable String orderId) {
        return payments.getOrder(orderId);
    }

    @PostMapping("/{orderId}/pay")
    PaymentOrderResponse pay(@PathVariable String orderId) {
        return payments.pay(orderId);
    }

    @GetMapping("/status/{transactionId}")
    TransactionStatusResponse status(@PathVariable String transactionId) {
        return payments.status(transactionId);
    }

    @GetMapping("/merchant/{merchantId}")
    Page<PaymentOrderResponse> merchant(@PathVariable String merchantId, Pageable pageable) {
        return payments.merchantOrders(merchantId, pageable);
    }

    @GetMapping("/internal/transactions/{transactionId}")
    TransactionSnapshotResponse transaction(@PathVariable String transactionId) {
        return payments.transaction(transactionId);
    }

    @GetMapping("/internal/transactions")
    List<TransactionSnapshotResponse> successful(@RequestParam String merchantId, @RequestParam Instant from, @RequestParam Instant to) {
        return payments.successfulTransactions(merchantId, from, to);
    }

    @PostMapping("/internal/transactions/{transactionId}/status")
    TransactionStatusResponse update(@PathVariable String transactionId, @Valid @RequestBody PaymentStatusUpdateRequest request) {
        return payments.updateStatus(transactionId, request);
    }
}
