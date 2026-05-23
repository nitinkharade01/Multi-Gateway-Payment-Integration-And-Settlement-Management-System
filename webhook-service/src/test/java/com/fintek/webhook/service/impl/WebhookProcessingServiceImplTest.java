package com.fintek.webhook.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fintek.webhook.config.GatewaySecrets;
import com.fintek.webhook.dto.response.WebhookResponse;
import com.fintek.webhook.enums.*;
import com.fintek.webhook.exception.WebhookException;
import com.fintek.webhook.mapper.WebhookEventMapper;
import com.fintek.webhook.repository.WebhookEventRepository;
import com.fintek.webhook.service.PaymentStatusClient;
import com.fintek.webhook.util.HmacSignatures;
import com.fintek.webhook.validator.WebhookSignatureValidator;
import java.time.Instant;
import org.junit.jupiter.api.*;
import org.springframework.kafka.core.KafkaTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class WebhookProcessingServiceImplTest {
    private static final String SECRET = "webhook-secret";
    private WebhookEventRepository events;
    private PaymentStatusClient payments;
    private WebhookProcessingServiceImpl service;
    private KafkaTemplate<String, Object> kafka;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        events = mock(WebhookEventRepository.class);
        payments = mock(PaymentStatusClient.class);
        kafka = mock(KafkaTemplate.class);
        service = new WebhookProcessingServiceImpl(new ObjectMapper(), new WebhookSignatureValidator(300),
                new GatewaySecrets(SECRET, SECRET, SECRET), events, new WebhookEventMapper(), payments, kafka);
    }

    @Test
    void validWebhookSignatureAccepted() {
        String timestamp = String.valueOf(Instant.now().getEpochSecond());
        when(events.existsByGatewayAndGatewayEventId(GatewayName.RAZORPAY, "evt_1")).thenReturn(false);

        WebhookResponse response = service.process(GatewayName.RAZORPAY, payload(), signature(timestamp), timestamp);

        assertEquals(WebhookProcessingStatus.ACCEPTED, response.status());
        verify(payments).applyGatewayStatus(any());
        verify(events).save(any());
    }

    @Test
    void invalidWebhookSignatureRejected() {
        String timestamp = String.valueOf(Instant.now().getEpochSecond());
        assertThrows(WebhookException.class, () -> service.process(GatewayName.RAZORPAY, payload(), "bad", timestamp));
    }

    @Test
    void oldWebhookTimestampRejected() {
        String timestamp = String.valueOf(Instant.now().minusSeconds(400).getEpochSecond());
        assertThrows(WebhookException.class, () -> service.process(GatewayName.RAZORPAY, payload(), signature(timestamp), timestamp));
    }

    @Test
    void duplicateWebhookIgnored() {
        String timestamp = String.valueOf(Instant.now().getEpochSecond());
        when(events.existsByGatewayAndGatewayEventId(GatewayName.RAZORPAY, "evt_1")).thenReturn(true);

        WebhookResponse response = service.process(GatewayName.RAZORPAY, payload(), signature(timestamp), timestamp);

        assertEquals(WebhookProcessingStatus.DUPLICATE, response.status());
        verifyNoInteractions(payments);
    }

    private String signature(String timestamp) {
        return HmacSignatures.sha256(SECRET, timestamp + "." + payload());
    }

    private String payload() {
        return """
                {"event_id":"evt_1","transaction_id":"txn_1","order_id":"ord_1","merchant_id":"mrc_1",
                 "amount":125.00,"currency":"INR","status":"SUCCESS","merchant_webhook_url":"https://merchant.test/hook"}
                """;
    }
}
