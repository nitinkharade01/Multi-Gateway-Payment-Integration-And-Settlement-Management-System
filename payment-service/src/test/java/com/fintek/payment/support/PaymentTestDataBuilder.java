package com.fintek.payment.support;

import com.fintek.payment.dto.request.CreatePaymentOrderRequest;
import com.fintek.payment.dto.response.MerchantCredentialResponse;
import com.fintek.payment.entity.PaymentOrder;
import com.fintek.payment.entity.TransactionEntity;
import com.fintek.payment.enums.PaymentMode;
import com.fintek.payment.enums.PaymentStatus;
import com.fintek.payment.util.PaymentFingerprints;
import java.math.BigDecimal;
import java.time.Instant;

public final class PaymentTestDataBuilder {
    private PaymentTestDataBuilder() {
    }

    public static CreatePaymentOrderRequest createOrderRequest(String idempotencyKey, BigDecimal amount) {
        return new CreatePaymentOrderRequest("mrc_1", "pk_live_test", "sk_live_test", idempotencyKey,
                amount, "INR", PaymentMode.UPI, "customer@example.test", "+919876543210",
                "https://merchant.test/return");
    }

    public static MerchantCredentialResponse activeMerchant(BigDecimal limit) {
        return new MerchantCredentialResponse(true, "mrc_1", "ACTIVE", "https://merchant.test/webhook",
                limit, "credential accepted");
    }

    public static PaymentOrder order(String orderId, String idempotencyKey, PaymentStatus status) {
        CreatePaymentOrderRequest request = createOrderRequest(idempotencyKey, new BigDecimal("100.00"));
        PaymentOrder order = new PaymentOrder();
        order.setId("db_" + orderId);
        order.setOrderId(orderId);
        order.setMerchantId("mrc_1");
        order.setIdempotencyKey(idempotencyKey);
        order.setRequestFingerprint(PaymentFingerprints.from(request, "INR"));
        order.setAmount(request.amount());
        order.setCurrency("INR");
        order.setPaymentMode(PaymentMode.UPI);
        order.setStatus(status);
        order.setCustomerEmail(request.customerEmail());
        order.setCustomerPhone(request.customerPhone());
        order.setReturnUrl(request.returnUrl());
        order.setMerchantWebhookUrl("https://merchant.test/webhook");
        order.setCreatedAt(Instant.parse("2026-05-01T10:15:30Z"));
        order.setUpdatedAt(order.getCreatedAt());
        order.setExpiresAt(order.getCreatedAt().plusSeconds(900));
        return order;
    }

    public static TransactionEntity transaction(PaymentOrder order, String transactionId, PaymentStatus status) {
        TransactionEntity tx = new TransactionEntity();
        tx.setId("db_" + transactionId);
        tx.setOrder(order);
        tx.setTransactionId(transactionId);
        tx.setMerchantId(order.getMerchantId());
        tx.setAmount(order.getAmount());
        tx.setCurrency(order.getCurrency());
        tx.setStatus(status);
        tx.setGateway("CASHFREE_SIMULATOR");
        tx.setCheckoutUrl("https://checkout.test/" + transactionId);
        tx.setCreatedAt(order.getCreatedAt());
        tx.setUpdatedAt(order.getUpdatedAt());
        return tx;
    }
}
