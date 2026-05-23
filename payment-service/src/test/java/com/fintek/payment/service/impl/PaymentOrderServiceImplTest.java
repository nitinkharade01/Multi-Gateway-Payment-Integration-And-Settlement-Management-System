package com.fintek.payment.service.impl;

import com.fintek.payment.dto.request.CreatePaymentOrderRequest;
import com.fintek.payment.dto.response.*;
import com.fintek.payment.entity.*;
import com.fintek.payment.enums.*;
import com.fintek.payment.exception.*;
import com.fintek.payment.mapper.PaymentMapper;
import com.fintek.payment.repository.*;
import com.fintek.payment.service.*;
import com.fintek.payment.validator.PaymentOrderValidator;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PaymentOrderServiceImplTest {
    private PaymentOrderRepository orders;
    private TransactionRepository transactions;
    private MerchantCredentialClient merchantClient;
    private GatewayRoutingClient routingClient;
    private PaymentEventPublisher events;
    private PaymentOrderServiceImpl service;

    @BeforeEach
    void setUp() {
        orders = mock(PaymentOrderRepository.class);
        transactions = mock(TransactionRepository.class);
        merchantClient = mock(MerchantCredentialClient.class);
        routingClient = mock(GatewayRoutingClient.class);
        events = mock(PaymentEventPublisher.class);
        service = new PaymentOrderServiceImpl(orders, transactions, merchantClient, routingClient,
                new PaymentOrderValidator(), new PaymentMapper(), events);
        when(merchantClient.validate(anyString(), anyString())).thenReturn(
                new MerchantCredentialResponse(true, "mrc_1", "ACTIVE", "https://merchant.test/webhook",
                        new BigDecimal("100000.00"), "ok"));
    }

    @Test
    void paymentOrderSuccessPersistsTransactionAndCheckoutRoute() {
        when(orders.findByMerchantIdAndIdempotencyKey("mrc_1", "idem-1")).thenReturn(Optional.empty());
        when(routingClient.route(any())).thenReturn(new GatewayRouteResponse("CASHFREE_SIMULATOR",
                "https://checkout.test/txn", false, "preferred"));
        when(transactions.save(any())).thenAnswer(answer -> answer.getArgument(0));

        PaymentOrderResponse response = service.create(request("idem-1", "1200.00"));

        assertEquals(PaymentStatus.PENDING, response.status());
        assertEquals("CASHFREE_SIMULATOR", response.gateway());
        assertFalse(response.idempotentReplay());
        verify(orders).saveAndFlush(any(PaymentOrder.class));
        verify(events).paymentCreated(any());
    }

    @Test
    void duplicateIdempotencyKeyReturnsExistingOrder() {
        Fixture fixture = existing("idem-2", "1200.00");
        when(orders.findByMerchantIdAndIdempotencyKey("mrc_1", "idem-2")).thenReturn(Optional.of(fixture.order()));
        when(transactions.findFirstByOrderOrderIdOrderByCreatedAtDesc(fixture.order().getOrderId())).thenReturn(Optional.of(fixture.tx()));

        PaymentOrderResponse response = service.create(request("idem-2", "1200.00"));

        assertTrue(response.idempotentReplay());
        assertEquals(fixture.tx().getTransactionId(), response.transactionId());
        verify(routingClient, never()).route(any());
    }

    @Test
    void idempotencyConflictThrowsWhenPayloadChanges() {
        Fixture fixture = existing("idem-3", "1200.00");
        when(orders.findByMerchantIdAndIdempotencyKey("mrc_1", "idem-3")).thenReturn(Optional.of(fixture.order()));

        assertThrows(IdempotencyConflictException.class, () -> service.create(request("idem-3", "1300.00")));
    }

    @Test
    void invalidMerchantApiKeyIsRejected() {
        when(merchantClient.validate(anyString(), anyString())).thenThrow(new PaymentException(401, "invalid key"));

        PaymentException error = assertThrows(PaymentException.class, () -> service.create(request("idem-4", "100.00")));

        assertEquals(401, error.status());
        verifyNoInteractions(routingClient);
    }

    @Test
    void inactiveMerchantIsRejected() {
        when(merchantClient.validate(anyString(), anyString())).thenThrow(new PaymentException(403, "inactive merchant"));

        PaymentException error = assertThrows(PaymentException.class, () -> service.create(request("idem-5", "100.00")));

        assertEquals(403, error.status());
    }

    private CreatePaymentOrderRequest request(String idempotency, String amount) {
        return new CreatePaymentOrderRequest("mrc_1", "pk", "sk", idempotency, new BigDecimal(amount), "INR",
                PaymentMode.UPI, "customer@example.test", "+919876543210", "https://merchant.test/return");
    }

    private Fixture existing(String idempotency, String amount) {
        CreatePaymentOrderRequest request = request(idempotency, amount);
        PaymentOrder order = new PaymentOrder();
        order.setId("order-db");
        order.setOrderId("ord_existing");
        order.setMerchantId("mrc_1");
        order.setIdempotencyKey(idempotency);
        order.setRequestFingerprint(com.fintek.payment.util.PaymentFingerprints.from(request, "INR"));
        order.setAmount(new BigDecimal(amount));
        order.setCurrency("INR");
        order.setPaymentMode(PaymentMode.UPI);
        order.setStatus(PaymentStatus.PENDING);
        order.setCreatedAt(Instant.now());
        order.setUpdatedAt(Instant.now());
        order.setExpiresAt(Instant.now().plusSeconds(60));
        TransactionEntity tx = new TransactionEntity();
        tx.setOrder(order);
        tx.setTransactionId("txn_existing");
        tx.setMerchantId("mrc_1");
        tx.setAmount(order.getAmount());
        tx.setCurrency("INR");
        tx.setStatus(PaymentStatus.PENDING);
        tx.setGateway("CASHFREE_SIMULATOR");
        tx.setCheckoutUrl("https://checkout.test/existing");
        tx.setCreatedAt(Instant.now());
        tx.setUpdatedAt(Instant.now());
        return new Fixture(order, tx);
    }

    private record Fixture(PaymentOrder order, TransactionEntity tx) {
    }
}
