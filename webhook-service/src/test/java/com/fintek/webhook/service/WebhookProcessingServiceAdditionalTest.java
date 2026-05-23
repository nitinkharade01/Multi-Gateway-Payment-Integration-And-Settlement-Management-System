package com.fintek.webhook.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fintek.common.events.PaymentFailedEvent;
import com.fintek.common.events.PaymentSuccessEvent;
import com.fintek.webhook.config.GatewaySecrets;
import com.fintek.webhook.enums.GatewayName;
import com.fintek.webhook.exception.WebhookException;
import com.fintek.webhook.mapper.WebhookEventMapper;
import com.fintek.webhook.repository.WebhookEventRepository;
import com.fintek.webhook.service.impl.WebhookProcessingServiceImpl;
import com.fintek.webhook.support.WebhookTestDataBuilder;
import com.fintek.webhook.validator.WebhookSignatureValidator;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebhookProcessingServiceAdditionalTest {
    @Mock
    private WebhookEventRepository events;
    @Mock
    private PaymentStatusClient paymentStatusClient;
    @Mock
    private KafkaTemplate<String, Object> kafka;

    private WebhookProcessingServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new WebhookProcessingServiceImpl(new ObjectMapper(), new WebhookSignatureValidator(300),
                new GatewaySecrets(WebhookTestDataBuilder.SECRET, WebhookTestDataBuilder.SECRET,
                        WebhookTestDataBuilder.SECRET), events, new WebhookEventMapper(), paymentStatusClient, kafka);
    }

    @Test
    void shouldProcessValidCashfreeWebhookAndPublishPaymentSuccessEvent() {
        String timestamp = String.valueOf(Instant.now().getEpochSecond());

        service.process(GatewayName.CASHFREE, WebhookTestDataBuilder.successfulPayload(),
                WebhookTestDataBuilder.signature(timestamp, WebhookTestDataBuilder.successfulPayload()), timestamp);

        ArgumentCaptor<Object> event = ArgumentCaptor.forClass(Object.class);
        verify(kafka).send(eq("payment-events"), eq("mrc_1"), event.capture());
        assertInstanceOf(PaymentSuccessEvent.class, event.getValue(),
                "Successful gateway webhook should publish payment success event");
        verify(paymentStatusClient).applyGatewayStatus(any());
        verify(events).save(argThat(saved -> saved.getRawPayload().contains("\"event_id\":\"evt_1\"")));
    }

    @Test
    void shouldProcessValidPayuWebhookAndPublishPaymentFailedEvent() {
        String timestamp = String.valueOf(Instant.now().getEpochSecond());

        service.process(GatewayName.PAYU, WebhookTestDataBuilder.failedPayload(),
                WebhookTestDataBuilder.signature(timestamp, WebhookTestDataBuilder.failedPayload()), timestamp);

        ArgumentCaptor<Object> event = ArgumentCaptor.forClass(Object.class);
        verify(kafka).send(eq("payment-events"), eq("mrc_1"), event.capture());
        assertInstanceOf(PaymentFailedEvent.class, event.getValue(),
                "Failed gateway webhook should publish payment failed event");
    }

    @Test
    void shouldHandleMalformedPayloadAndInvalidCurrency() {
        String timestamp = String.valueOf(Instant.now().getEpochSecond());
        assertThrows(WebhookException.class,
                () -> service.process(GatewayName.RAZORPAY, "{bad-json}",
                        WebhookTestDataBuilder.signature(timestamp, "{bad-json}"), timestamp),
                "Malformed JSON should be rejected");

        String usdPayload = WebhookTestDataBuilder.successfulPayload().replace("\"INR\"", "\"USD\"");
        assertThrows(WebhookException.class,
                () -> service.process(GatewayName.RAZORPAY, usdPayload,
                        WebhookTestDataBuilder.signature(timestamp, usdPayload), timestamp),
                "Non-INR webhook payloads should be rejected");
    }
}
