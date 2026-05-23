package com.fintek.refund.service.impl;

import com.fintek.common.events.RefundSuccessEvent;
import com.fintek.common.money.Money;
import com.fintek.refund.dto.request.CreateRefundRequest;
import com.fintek.refund.dto.response.*;
import com.fintek.refund.entity.RefundRecord;
import com.fintek.refund.enums.*;
import com.fintek.refund.exception.RefundException;
import com.fintek.refund.mapper.RefundMapper;
import com.fintek.refund.repository.RefundRepository;
import com.fintek.refund.service.*;
import com.fintek.refund.util.GatewayRefundSimulator;
import com.fintek.refund.util.RefundIds;
import com.fintek.refund.validator.RefundValidator;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RefundServiceImpl implements RefundService {
    private static final Logger log = LoggerFactory.getLogger(RefundServiceImpl.class);
    private final RefundRepository refunds;
    private final PaymentRefundClient paymentClient;
    private final GatewayRefundSimulator gateway;
    private final RefundValidator validator;
    private final RefundMapper mapper;
    private final RefundEventPublisher eventPublisher;

    public RefundServiceImpl(RefundRepository refunds, PaymentRefundClient paymentClient, GatewayRefundSimulator gateway,
                             RefundValidator validator, RefundMapper mapper, RefundEventPublisher eventPublisher) {
        this.refunds = refunds;
        this.paymentClient = paymentClient;
        this.gateway = gateway;
        this.validator = validator;
        this.mapper = mapper;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public RefundResponse create(CreateRefundRequest request) {
        PaymentTransactionSnapshot paid = paymentClient.transaction(request.transactionId());
        BigDecimal successfulRefunded = refundedAmount(request.transactionId());
        BigDecimal refundable = Money.scale(paid.amount().subtract(successfulRefunded));
        validator.validate(request, paid, refundable);
        Instant now = Instant.now();
        RefundRecord refund = new RefundRecord();
        refund.setId(UUID.randomUUID().toString());
        refund.setRefundId(RefundIds.refundId());
        refund.setMerchantId(request.merchantId());
        refund.setTransactionId(request.transactionId());
        refund.setAmount(Money.scale(request.amount()));
        refund.setReason(request.reason().trim());
        refund.setStatus(RefundStatus.REFUND_PROCESSING);
        refund.setCreatedAt(now);
        refund.setUpdatedAt(now);
        refunds.save(refund);
        refund.setGatewayReference(gateway.refund(refund));
        refund.setStatus(RefundStatus.REFUND_SUCCESS);
        refund.setUpdatedAt(Instant.now());
        refunds.save(refund);
        BigDecimal remaining = Money.scale(refundable.subtract(refund.getAmount()));
        paymentClient.update(paid.transactionId(), remaining.signum() == 0 ? PaymentStatus.REFUNDED : PaymentStatus.PARTIALLY_REFUNDED);
        eventPublisher.publish(new RefundSuccessEvent(UUID.randomUUID().toString(), Instant.now(), paid.merchantId(),
                refund.getRefundId(), paid.transactionId(), refund.getAmount()));
        log.info("Refund {} succeeded for {} remaining refundable {}", refund.getRefundId(), paid.transactionId(), remaining);
        return mapper.response(refund, remaining);
    }

    @Override
    @Transactional(readOnly = true)
    public RefundResponse get(String refundId) {
        RefundRecord refund = refunds.findByRefundId(refundId).orElseThrow(() -> new RefundException(404, "Refund not found"));
        PaymentTransactionSnapshot paid = paymentClient.transaction(refund.getTransactionId());
        return mapper.response(refund, Money.scale(paid.amount().subtract(refundedAmount(refund.getTransactionId()))));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RefundResponse> merchantRefunds(String merchantId, Pageable pageable) {
        return refunds.findByMerchantId(merchantId, pageable).map(refund -> {
            PaymentTransactionSnapshot paid = paymentClient.transaction(refund.getTransactionId());
            return mapper.response(refund, Money.scale(paid.amount().subtract(refundedAmount(refund.getTransactionId()))));
        });
    }

    private BigDecimal refundedAmount(String transactionId) {
        return refunds.findByTransactionIdAndStatusIn(transactionId, List.of(RefundStatus.REFUND_SUCCESS)).stream()
                .map(RefundRecord::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
