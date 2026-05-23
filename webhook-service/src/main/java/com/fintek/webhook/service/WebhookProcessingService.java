package com.fintek.webhook.service;

import com.fintek.webhook.dto.response.WebhookResponse;
import com.fintek.webhook.enums.GatewayName;

public interface WebhookProcessingService {
    WebhookResponse process(GatewayName gateway, String payload, String signature, String timestamp);
}
