package com.fintek.payment.service;

import com.fintek.payment.dto.request.PaymentStatusUpdateRequest;
import com.fintek.payment.dto.response.GatewayRouteResponse;
import com.fintek.payment.enums.PaymentStatus;
import com.fintek.payment.exception.PaymentException;
import com.fintek.payment.mapper.PaymentMapper;
import com.fintek.payment.repository.*;
import com.fintek.payment.service.impl.PaymentEventPublisher;
import com.fintek.payment.service.impl.PaymentOrderServiceImpl;
import com.fintek.payment.support.PaymentTestDataBuilder;
import com.fintek.payment.validator.PaymentOrderValidator;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentOrderServiceAdditionalTest {
    @Mock
    private PaymentOrderRepository orders;
    @Mock
    private TransactionRepository transactions;
    @Mock
    private MerchantCredentialClient merchantClient;
    @Mock
    private GatewayRoutingClient routingClient;
    @Mock
    private PaymentEventPublisher events;

    private PaymentOrderServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new PaymentOrderServiceImpl(orders, transactions, merchantClient, routingClient,
                new PaymentOrderValidator(), new PaymentMapper(), events);
        lenient().when(merchantClient.validate(anyString(), anyString()))
                .thenReturn(PaymentTestDataBuilder.activeMerchant(new BigDecimal("1000.00")));
    }

    @Test
    void shouldRejectAmountLessThanOrEqualToZeroAndAboveMerchantLimit() {
        PaymentException zero = assertThrows(PaymentException.class,
                () -> service.create(PaymentTestDataBuilder.createOrderRequest("idem-zero", BigDecimal.ZERO)),
                "Zero amount should be rejected");
        assertEquals(400, zero.status(), "Zero amount should map to bad request");

        PaymentException aboveLimit = assertThrows(PaymentException.class,
                () -> service.create(PaymentTestDataBuilder.createOrderRequest("idem-high", new BigDecimal("1000.01"))),
                "Amounts above merchant limit should be rejected");
        assertEquals(422, aboveLimit.status(), "Limit breach should map to unprocessable entity");
    }

    @Test
    void shouldRejectUnsupportedCurrencyAndMerchantCredentialMismatch() {
        var unsupportedCurrency = new com.fintek.payment.dto.request.CreatePaymentOrderRequest("mrc_1", "pk", "sk",
                "idem-currency", new BigDecimal("100.00"), "USD", com.fintek.payment.enums.PaymentMode.UPI,
                "customer@example.test", "+919876543210", null);

        assertThrows(PaymentException.class, () -> service.create(unsupportedCurrency),
                "Unsupported currency should be rejected");

        when(merchantClient.validate(anyString(), anyString())).thenReturn(
                new com.fintek.payment.dto.response.MerchantCredentialResponse(true, "mrc_2", "ACTIVE",
                        "https://merchant.test/webhook", new BigDecimal("1000.00"), "ok"));

        PaymentException mismatch = assertThrows(PaymentException.class,
                () -> service.create(PaymentTestDataBuilder.createOrderRequest("idem-mismatch", new BigDecimal("100.00"))),
                "Credentials for another merchant should be rejected");
        assertEquals(403, mismatch.status(), "Merchant mismatch should map to forbidden");
    }

    @Test
    void shouldGenerateUniqueOrderAndTransactionIdsAndCallGatewayRoutingService() {
        when(orders.findByMerchantIdAndIdempotencyKey("mrc_1", "idem-unique")).thenReturn(Optional.empty());
        when(routingClient.route(any())).thenReturn(new GatewayRouteResponse("CASHFREE_SIMULATOR",
                "https://checkout.test/txn", false, "preferred"));
        when(transactions.save(any())).thenAnswer(answer -> answer.getArgument(0));

        var response = service.create(PaymentTestDataBuilder.createOrderRequest("idem-unique", new BigDecimal("100.00")));

        assertTrue(response.orderId().startsWith("ord_"), "Payment order ID should use order prefix");
        assertTrue(response.transactionId().startsWith("txn_"), "Transaction ID should use transaction prefix");
        assertEquals(PaymentStatus.PENDING, response.status(), "New routed orders should be pending");
        assertEquals("https://checkout.test/txn", response.checkoutUrl(), "Checkout URL should come from routing service");
        verify(routingClient).route(any());
        verify(events).paymentCreated(any());
    }

    @Test
    void shouldReturnNotFoundForInvalidTransactionId() {
        when(transactions.findByTransactionId("missing")).thenReturn(Optional.empty());

        PaymentException error = assertThrows(PaymentException.class, () -> service.status("missing"),
                "Unknown transaction ID should fail status lookup");

        assertEquals(404, error.status(), "Unknown transaction should map to not found");
    }

    @Test
    void shouldMarkPaymentAsExpiredWhenPastExpiryTime() {
        var order = PaymentTestDataBuilder.order("ord_expired", "idem-expired", PaymentStatus.PENDING);
        order.setExpiresAt(Instant.now().minusSeconds(1));
        var tx = PaymentTestDataBuilder.transaction(order, "txn_expired", PaymentStatus.PENDING);
        when(orders.findByOrderId("ord_expired")).thenReturn(Optional.of(order));
        when(transactions.findFirstByOrderOrderIdOrderByCreatedAtDesc("ord_expired")).thenReturn(Optional.of(tx));

        var response = service.getOrder("ord_expired");

        assertEquals(PaymentStatus.EXPIRED, response.status(), "Expired unpaid orders should be marked expired");
        verify(orders).save(order);
        verify(transactions).save(tx);
    }

    @Test
    void shouldHandlePaymentFailureReasonAndRejectGatewayCallbackForRefundedTransaction() {
        var order = PaymentTestDataBuilder.order("ord_1", "idem-1", PaymentStatus.PENDING);
        var tx = PaymentTestDataBuilder.transaction(order, "txn_1", PaymentStatus.PENDING);
        when(transactions.findByTransactionId("txn_1")).thenReturn(Optional.of(tx));
        when(transactions.save(tx)).thenReturn(tx);

        var response = service.updateStatus("txn_1", new PaymentStatusUpdateRequest(PaymentStatus.FAILED, "bank declined"));

        assertEquals(PaymentStatus.FAILED, response.status(), "Gateway failed status should be applied");
        assertEquals("bank declined", response.failureReason(), "Failure reason should be preserved");

        tx.setStatus(PaymentStatus.REFUNDED);
        assertThrows(PaymentException.class,
                () -> service.updateStatus("txn_1", new PaymentStatusUpdateRequest(PaymentStatus.SUCCESS, null)),
                "Refunded transactions should not accept later gateway status overrides");
    }

    @Test
    void shouldReturnMerchantOrdersAndSuccessfulTransactions() {
        var order = PaymentTestDataBuilder.order("ord_1", "idem-1", PaymentStatus.SUCCESS);
        var tx = PaymentTestDataBuilder.transaction(order, "txn_1", PaymentStatus.SUCCESS);
        when(orders.findByMerchantId(eq("mrc_1"), any())).thenReturn(new org.springframework.data.domain.PageImpl<>(List.of(order)));
        when(transactions.findFirstByOrderOrderIdOrderByCreatedAtDesc("ord_1")).thenReturn(Optional.of(tx));
        when(transactions.findByMerchantIdAndCreatedAtBetweenAndStatus(eq("mrc_1"), any(), any(), eq(PaymentStatus.SUCCESS)))
                .thenReturn(List.of(tx));

        assertEquals(1, service.merchantOrders("mrc_1", PageRequest.of(0, 10)).getTotalElements(),
                "Merchant order page should include saved order");
        assertEquals(1, service.successfulTransactions("mrc_1", Instant.now().minusSeconds(60), Instant.now()).size(),
                "Successful transaction snapshot list should include captured transaction");
    }
}
