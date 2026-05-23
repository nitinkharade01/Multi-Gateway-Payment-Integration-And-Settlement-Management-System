package com.fintek.notification.service.impl;

import com.fintek.common.events.*;
import com.fintek.notification.dto.response.NotificationResponse;
import com.fintek.notification.entity.NotificationLog;
import com.fintek.notification.enums.*;
import com.fintek.notification.mapper.NotificationMapper;
import com.fintek.notification.repository.NotificationLogRepository;
import com.fintek.notification.service.NotificationService;
import com.fintek.notification.util.NotificationTemplates;
import com.fintek.notification.validator.NotificationEventValidator;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class NotificationServiceImpl implements NotificationService {
    private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);
    private final NotificationLogRepository logs;
    private final NotificationMapper mapper;
    private final NotificationEventValidator validator;
    private final ExecutorService executor;

    public NotificationServiceImpl(NotificationLogRepository logs, NotificationMapper mapper,
                                   NotificationEventValidator validator, ExecutorService executor) {
        this.logs = logs;
        this.mapper = mapper;
        this.validator = validator;
        this.executor = executor;
    }

    @Override
    public void handle(PaymentEvent event) {
        validator.validate(event);
        EventContext context = context(event);
        executor.submit(() -> simulateEmail(event.eventId(), context));
        executor.submit(() -> simulateWebhook(event.eventId(), context));
    }

    @Override
    public Page<NotificationResponse> merchantLogs(String merchantId, Pageable pageable) {
        return logs.findByMerchantId(merchantId, pageable).map(mapper::response);
    }

    private EventContext context(PaymentEvent event) {
        if (event instanceof PaymentSuccessEvent success) {
            return new EventContext(success.merchantId(), success.merchantWebhookUrl(), "PAYMENT_SUCCESS",
                    "Transaction " + success.transactionId() + " captured for " + success.amount());
        }
        if (event instanceof PaymentFailedEvent failed) {
            return new EventContext(failed.merchantId(), failed.merchantWebhookUrl(), "PAYMENT_FAILED",
                    "Transaction " + failed.transactionId() + " failed: " + failed.reason());
        }
        if (event instanceof PaymentCreatedEvent created) {
            return new EventContext(created.merchantId(), null, "PAYMENT_CREATED",
                    "Checkout created for order " + created.orderId());
        }
        if (event instanceof RefundSuccessEvent refund) {
            return new EventContext(refund.merchantId(), null, "REFUND_SUCCESS",
                    "Refund " + refund.refundId() + " succeeded for " + refund.amount());
        }
        if (event instanceof SettlementGeneratedEvent settlement) {
            return new EventContext(settlement.merchantId(), null, "SETTLEMENT_GENERATED",
                    "Settlement " + settlement.settlementId() + " net amount " + settlement.netAmount());
        }
        if (event instanceof FraudAlertCreatedEvent fraud) {
            return new EventContext(fraud.merchantId(), null, "FRAUD_ALERT",
                    "Risk " + fraud.riskLevel() + " score " + fraud.score() + " rules " + fraud.reasons());
        }
        throw new IllegalArgumentException("Unsupported payment event " + event.getClass().getName());
    }

    private void simulateEmail(String eventId, EventContext context) {
        String message = NotificationTemplates.EMAIL.formatted(context.merchantId(), context.type(), context.detail());
        log.info("Simulated merchant email for {} type {}", context.merchantId(), context.type());
        save(eventId, context.merchantId(), NotificationChannel.EMAIL, NotificationStatus.SIMULATED,
                "merchant-contact:" + context.merchantId(), message);
    }

    private void simulateWebhook(String eventId, EventContext context) {
        String json = NotificationTemplates.WEBHOOK_JSON.formatted(eventId, context.merchantId(), context.type(),
                context.detail().replace("\"", "'"));
        if (context.webhookUrl() == null || context.webhookUrl().isBlank()) {
            save(eventId, context.merchantId(), NotificationChannel.MERCHANT_WEBHOOK, NotificationStatus.SKIPPED,
                    "unconfigured", json);
            return;
        }
        log.info("Simulated merchant webhook callback to {} for {}", context.webhookUrl(), eventId);
        save(eventId, context.merchantId(), NotificationChannel.MERCHANT_WEBHOOK, NotificationStatus.SIMULATED,
                context.webhookUrl(), json);
    }

    private void save(String eventId, String merchantId, NotificationChannel channel, NotificationStatus status,
                      String destination, String message) {
        NotificationLog record = new NotificationLog();
        record.setId(UUID.randomUUID().toString());
        record.setEventId(eventId);
        record.setMerchantId(merchantId);
        record.setChannel(channel);
        record.setStatus(status);
        record.setDestination(destination);
        record.setRenderedMessage(message);
        record.setCreatedAt(Instant.now());
        logs.save(record);
    }

    private record EventContext(String merchantId, String webhookUrl, String type, String detail) {
    }
}
