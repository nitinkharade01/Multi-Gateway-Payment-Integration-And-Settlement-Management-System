package com.fintek.payment.service.impl;

import com.fintek.common.events.PaymentCreatedEvent;
import com.fintek.common.money.Money;
import com.fintek.payment.dto.request.*;
import com.fintek.payment.dto.response.*;
import com.fintek.payment.entity.PaymentOrder;
import com.fintek.payment.entity.TransactionEntity;
import com.fintek.payment.enums.PaymentStatus;
import com.fintek.payment.exception.IdempotencyConflictException;
import com.fintek.payment.exception.PaymentException;
import com.fintek.payment.mapper.PaymentMapper;
import com.fintek.payment.repository.PaymentOrderRepository;
import com.fintek.payment.repository.TransactionRepository;
import com.fintek.payment.service.*;
import com.fintek.payment.util.PaymentFingerprints;
import com.fintek.payment.util.PaymentIds;
import com.fintek.payment.validator.PaymentOrderValidator;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentOrderServiceImpl implements PaymentOrderService {
    private static final Logger log = LoggerFactory.getLogger(PaymentOrderServiceImpl.class);
    private final PaymentOrderRepository orders;
    private final TransactionRepository transactions;
    private final MerchantCredentialClient merchantClient;
    private final GatewayRoutingClient routingClient;
    private final PaymentOrderValidator validator;
    private final PaymentMapper mapper;
    private final PaymentEventPublisher events;

    public PaymentOrderServiceImpl(PaymentOrderRepository orders, TransactionRepository transactions,
                                   MerchantCredentialClient merchantClient, GatewayRoutingClient routingClient,
                                   PaymentOrderValidator validator, PaymentMapper mapper, PaymentEventPublisher events) {
        this.orders = orders;
        this.transactions = transactions;
        this.merchantClient = merchantClient;
        this.routingClient = routingClient;
        this.validator = validator;
        this.mapper = mapper;
        this.events = events;
    }

    @Override
    @Transactional
    public PaymentOrderResponse create(CreatePaymentOrderRequest request) {
        String currency = validator.currency(request);
        MerchantCredentialResponse merchant = merchantClient.validate(request.apiKey(), request.apiSecret());
        if (!request.merchantId().equals(merchant.merchantId())) {
            throw new PaymentException(403, "API credentials do not belong to merchant " + request.merchantId());
        }
        validator.validateLimit(request, merchant);
        String fingerprint = PaymentFingerprints.from(request, currency);
        var existing = orders.findByMerchantIdAndIdempotencyKey(request.merchantId(), request.idempotencyKey());
        if (existing.isPresent()) {
            return replay(existing.get(), fingerprint);
        }
        PaymentOrder order = newOrder(request, merchant, fingerprint, currency);
        TransactionEntity tx = newTransaction(order);
        try {
            orders.saveAndFlush(order);
        } catch (DataIntegrityViolationException raced) {
            return replay(orders.findByMerchantIdAndIdempotencyKey(request.merchantId(), request.idempotencyKey())
                    .orElseThrow(() -> raced), fingerprint);
        }
        GatewayRouteResponse route = routingClient.route(new GatewayRouteRequest(order.getMerchantId(), order.getOrderId(),
                tx.getTransactionId(), order.getAmount(), order.getCurrency(), order.getPaymentMode()));
        tx.setGateway(route.gateway());
        tx.setCheckoutUrl(route.checkoutUrl());
        tx.setStatus(PaymentStatus.PENDING);
        tx.setUpdatedAt(Instant.now());
        order.setStatus(PaymentStatus.PENDING);
        order.setUpdatedAt(tx.getUpdatedAt());
        transactions.save(tx);
        events.paymentCreated(new PaymentCreatedEvent(UUID.randomUUID().toString(), Instant.now(), order.getMerchantId(),
                order.getOrderId(), tx.getTransactionId(), order.getAmount(), order.getCurrency()));
        log.info("Created payment order {} transaction {} using gateway {}", order.getOrderId(), tx.getTransactionId(), tx.getGateway());
        return mapper.order(order, tx, false);
    }

    @Override
    @Transactional
    public PaymentOrderResponse getOrder(String orderId) {
        PaymentOrder order = requireOrder(orderId);
        TransactionEntity tx = requireTransactionFor(orderId);
        expireIfNecessary(order, tx);
        return mapper.order(order, tx, false);
    }

    @Override
    @Transactional
    public PaymentOrderResponse pay(String orderId) {
        PaymentOrder order = requireOrder(orderId);
        TransactionEntity tx = requireTransactionFor(orderId);
        expireIfNecessary(order, tx);
        if (order.getStatus() == PaymentStatus.EXPIRED) {
            throw new PaymentException(409, "Payment order expired");
        }
        if (tx.getCheckoutUrl() == null) {
            GatewayRouteResponse route = routingClient.route(new GatewayRouteRequest(order.getMerchantId(), order.getOrderId(),
                    tx.getTransactionId(), order.getAmount(), order.getCurrency(), order.getPaymentMode()));
            tx.setGateway(route.gateway());
            tx.setCheckoutUrl(route.checkoutUrl());
            transactions.save(tx);
        }
        return mapper.order(order, tx, false);
    }

    @Override
    @Transactional
    public TransactionStatusResponse status(String transactionId) {
        TransactionEntity tx = requireTransaction(transactionId);
        expireIfNecessary(tx.getOrder(), tx);
        return mapper.status(tx);
    }

    @Override
    @Transactional
    public TransactionStatusResponse updateStatus(String transactionId, PaymentStatusUpdateRequest request) {
        TransactionEntity tx = requireTransaction(transactionId);
        if (tx.getStatus() == PaymentStatus.REFUNDED || tx.getStatus() == PaymentStatus.PARTIALLY_REFUNDED) {
            throw new PaymentException(409, "Refunded transaction status cannot be replaced by a gateway callback");
        }
        tx.setStatus(request.status());
        tx.setFailureReason(request.failureReason());
        tx.setUpdatedAt(Instant.now());
        tx.getOrder().setStatus(request.status());
        tx.getOrder().setUpdatedAt(tx.getUpdatedAt());
        log.info("Transaction {} moved to {}", transactionId, request.status());
        return mapper.status(transactions.save(tx));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentOrderResponse> merchantOrders(String merchantId, Pageable pageable) {
        return orders.findByMerchantId(merchantId, pageable)
                .map(order -> mapper.order(order, requireTransactionFor(order.getOrderId()), false));
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionSnapshotResponse transaction(String transactionId) {
        return mapper.snapshot(requireTransaction(transactionId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionSnapshotResponse> successfulTransactions(String merchantId, Instant from, Instant to) {
        return transactions.findByMerchantIdAndCreatedAtBetweenAndStatus(merchantId, from, to, PaymentStatus.SUCCESS)
                .stream().map(mapper::snapshot).toList();
    }

    private PaymentOrderResponse replay(PaymentOrder order, String fingerprint) {
        if (!fingerprint.equals(order.getRequestFingerprint())) {
            throw new IdempotencyConflictException("Idempotency key was already used with a different order payload");
        }
        log.info("Replayed existing order {} for idempotency key {}", order.getOrderId(), order.getIdempotencyKey());
        return mapper.order(order, requireTransactionFor(order.getOrderId()), true);
    }

    private PaymentOrder newOrder(CreatePaymentOrderRequest request, MerchantCredentialResponse merchant, String fingerprint, String currency) {
        Instant now = Instant.now();
        PaymentOrder order = new PaymentOrder();
        order.setId(UUID.randomUUID().toString());
        order.setOrderId(PaymentIds.orderId());
        order.setMerchantId(request.merchantId());
        order.setIdempotencyKey(request.idempotencyKey().trim());
        order.setRequestFingerprint(fingerprint);
        order.setAmount(Money.scale(request.amount()));
        order.setCurrency(currency);
        order.setPaymentMode(request.paymentMode());
        order.setStatus(PaymentStatus.CREATED);
        order.setCustomerEmail(request.customerEmail().trim().toLowerCase());
        order.setCustomerPhone(request.customerPhone().trim());
        order.setReturnUrl(request.returnUrl());
        order.setMerchantWebhookUrl(merchant.webhookUrl());
        order.setCreatedAt(now);
        order.setUpdatedAt(now);
        order.setExpiresAt(now.plus(15, ChronoUnit.MINUTES));
        return order;
    }

    private TransactionEntity newTransaction(PaymentOrder order) {
        TransactionEntity tx = new TransactionEntity();
        tx.setId(UUID.randomUUID().toString());
        tx.setOrder(order);
        tx.setTransactionId(PaymentIds.transactionId());
        tx.setMerchantId(order.getMerchantId());
        tx.setAmount(order.getAmount());
        tx.setCurrency(order.getCurrency());
        tx.setStatus(PaymentStatus.CREATED);
        tx.setCreatedAt(order.getCreatedAt());
        tx.setUpdatedAt(order.getCreatedAt());
        return tx;
    }

    private void expireIfNecessary(PaymentOrder order, TransactionEntity tx) {
        if (order.expiredAt(Instant.now())) {
            order.setStatus(PaymentStatus.EXPIRED);
            tx.setStatus(PaymentStatus.EXPIRED);
            tx.setUpdatedAt(Instant.now());
            orders.save(order);
            transactions.save(tx);
        }
    }

    private PaymentOrder requireOrder(String orderId) {
        return orders.findByOrderId(orderId).orElseThrow(() -> new PaymentException(404, "Payment order not found"));
    }

    private TransactionEntity requireTransactionFor(String orderId) {
        return transactions.findFirstByOrderOrderIdOrderByCreatedAtDesc(orderId)
                .orElseThrow(() -> new PaymentException(404, "Payment transaction not found"));
    }

    private TransactionEntity requireTransaction(String transactionId) {
        return transactions.findByTransactionId(transactionId)
                .orElseThrow(() -> new PaymentException(404, "Transaction not found"));
    }
}
