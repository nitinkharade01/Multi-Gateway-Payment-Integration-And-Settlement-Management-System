package com.fintek.webhook.service;

import com.fintek.webhook.dto.request.GatewayWebhookPayload;

public interface PaymentStatusClient {
    void applyGatewayStatus(GatewayWebhookPayload payload);
}
