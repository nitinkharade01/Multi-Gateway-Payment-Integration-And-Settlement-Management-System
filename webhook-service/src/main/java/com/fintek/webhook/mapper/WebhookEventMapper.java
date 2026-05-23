package com.fintek.webhook.mapper;

import com.fintek.webhook.dto.request.GatewayWebhookPayload;
import com.fintek.webhook.entity.WebhookEvent;
import com.fintek.webhook.enums.GatewayName;
import com.fintek.webhook.enums.WebhookProcessingStatus;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class WebhookEventMapper {
    public WebhookEvent accepted(GatewayName gateway, GatewayWebhookPayload payload, String rawBody,
                                 String signature, Instant gatewayTimestamp) {
        WebhookEvent event = new WebhookEvent();
        event.setId(UUID.randomUUID().toString());
        event.setGateway(gateway);
        event.setGatewayEventId(payload.eventId());
        event.setRawPayload(rawBody);
        event.setReceivedSignature(signature);
        event.setGatewayTimestamp(gatewayTimestamp);
        event.setTransactionId(payload.transactionId());
        event.setStatus(WebhookProcessingStatus.ACCEPTED);
        event.setProcessedAt(Instant.now());
        return event;
    }
}
