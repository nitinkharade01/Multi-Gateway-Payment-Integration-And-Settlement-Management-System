package com.fintek.webhook.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fintek.common.events.PaymentFailedEvent;
import com.fintek.common.events.PaymentSuccessEvent;
import com.fintek.webhook.config.GatewaySecrets;
import com.fintek.webhook.dto.request.GatewayWebhookPayload;
import com.fintek.webhook.dto.response.WebhookResponse;
import com.fintek.webhook.enums.GatewayName;
import com.fintek.webhook.enums.WebhookProcessingStatus;
import com.fintek.webhook.exception.WebhookException;
import com.fintek.webhook.mapper.WebhookEventMapper;
import com.fintek.webhook.repository.WebhookEventRepository;
import com.fintek.webhook.service.PaymentStatusClient;
import com.fintek.webhook.service.WebhookProcessingService;
import com.fintek.webhook.validator.WebhookSignatureValidator;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WebhookProcessingServiceImpl implements WebhookProcessingService {
    private static final Logger log = LoggerFactory.getLogger(WebhookProcessingServiceImpl.class);
    private final ObjectMapper objectMapper;
    private final WebhookSignatureValidator signatureValidator;
    private final GatewaySecrets gatewaySecrets;
    private final WebhookEventRepository events;
    private final WebhookEventMapper mapper;
    private final PaymentStatusClient paymentStatusClient;
    private final KafkaTemplate<String, Object> kafka;
    private final boolean kafkaEnabled;

    public WebhookProcessingServiceImpl(ObjectMapper objectMapper, WebhookSignatureValidator signatureValidator,
                                        GatewaySecrets gatewaySecrets, WebhookEventRepository events,
                                        WebhookEventMapper mapper, PaymentStatusClient paymentStatusClient,
                                        KafkaTemplate<String, Object> kafka) {
        this(objectMapper, signatureValidator, gatewaySecrets, events, mapper, paymentStatusClient, kafka, true);
    }

    @Autowired
    public WebhookProcessingServiceImpl(ObjectMapper objectMapper, WebhookSignatureValidator signatureValidator,
                                        GatewaySecrets gatewaySecrets, WebhookEventRepository events,
                                        WebhookEventMapper mapper, PaymentStatusClient paymentStatusClient,
                                        KafkaTemplate<String, Object> kafka,
                                        @Value("${app.kafka.enabled:true}") boolean kafkaEnabled) {
        this.objectMapper = objectMapper;
        this.signatureValidator = signatureValidator;
        this.gatewaySecrets = gatewaySecrets;
        this.events = events;
        this.mapper = mapper;
        this.paymentStatusClient = paymentStatusClient;
        this.kafka = kafka;
        this.kafkaEnabled = kafkaEnabled;
    }

    @Override
    @Transactional
    public WebhookResponse process(GatewayName gateway, String rawPayload, String signature, String timestamp) {
        Instant gatewayTimestamp = signatureValidator.validate(gatewaySecrets.forGateway(gateway), rawPayload, signature, timestamp);
        GatewayWebhookPayload payload = parse(rawPayload);
        requirePayload(payload);
        if (events.existsByGatewayAndGatewayEventId(gateway, payload.eventId())) {
            log.info("Ignoring duplicate webhook {} from {}", payload.eventId(), gateway);
            return new WebhookResponse(gateway, payload.eventId(), payload.transactionId(), WebhookProcessingStatus.DUPLICATE,
                    "Duplicate event already processed");
        }
        paymentStatusClient.applyGatewayStatus(payload);
        events.save(mapper.accepted(gateway, payload, rawPayload, signature, gatewayTimestamp));
        publish(payload);
        log.info("Accepted {} webhook {} for transaction {}", gateway, payload.eventId(), payload.transactionId());
        return new WebhookResponse(gateway, payload.eventId(), payload.transactionId(), WebhookProcessingStatus.ACCEPTED,
                "Webhook signature, timestamp and event ID accepted");
    }

    private GatewayWebhookPayload parse(String rawPayload) {
        try {
            return objectMapper.readValue(rawPayload, GatewayWebhookPayload.class);
        } catch (JsonProcessingException error) {
            throw new WebhookException(400, "Webhook payload is not valid JSON");
        }
    }

    private void requirePayload(GatewayWebhookPayload payload) {
        if (payload.eventId() == null || payload.eventId().isBlank() || payload.transactionId() == null
                || payload.transactionId().isBlank() || payload.merchantId() == null || payload.status() == null) {
            throw new WebhookException(400, "Webhook event ID, merchant, transaction and status are required");
        }
        if (!payload.currency().equals("INR")) {
            throw new WebhookException(400, "Webhook currency must be INR");
        }
    }

    private void publish(GatewayWebhookPayload payload) {
        if (!kafkaEnabled) {
            log.info("Kafka disabled; webhook event {} for transaction {} handled in demo mode",
                    payload.eventId(), payload.transactionId());
            return;
        }
        String eventId = UUID.randomUUID().toString();
        if ("SUCCESS".equals(payload.status().toUpperCase(Locale.ROOT))) {
            kafka.send("payment-events", payload.merchantId(), new PaymentSuccessEvent(eventId, Instant.now(), payload.merchantId(),
                    payload.orderId(), payload.transactionId(), payload.amount(), payload.currency(), payload.merchantWebhookUrl()));
        } else {
            kafka.send("payment-events", payload.merchantId(), new PaymentFailedEvent(eventId, Instant.now(), payload.merchantId(),
                    payload.orderId(), payload.transactionId(), payload.amount(), payload.currency(),
                    payload.reason() == null ? "gateway status " + payload.status() : payload.reason(), payload.merchantWebhookUrl()));
        }
    }
}
