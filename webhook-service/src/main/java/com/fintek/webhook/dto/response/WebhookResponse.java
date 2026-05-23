package com.fintek.webhook.dto.response;

import com.fintek.webhook.enums.GatewayName;
import com.fintek.webhook.enums.WebhookProcessingStatus;

public record WebhookResponse(GatewayName gateway, String eventId, String transactionId,
                              WebhookProcessingStatus status, String message) {
}
