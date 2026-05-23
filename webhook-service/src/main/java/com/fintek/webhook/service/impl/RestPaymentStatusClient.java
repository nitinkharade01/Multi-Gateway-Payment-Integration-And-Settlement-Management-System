package com.fintek.webhook.service.impl;

import com.fintek.webhook.dto.request.GatewayWebhookPayload;
import com.fintek.webhook.dto.request.PaymentStatusUpdateRequest;
import com.fintek.webhook.exception.WebhookException;
import com.fintek.webhook.service.PaymentStatusClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class RestPaymentStatusClient implements PaymentStatusClient {
    private final RestClient restClient;

    public RestPaymentStatusClient(RestClient.Builder builder, @Value("${services.payment-url}") String paymentUrl) {
        this.restClient = builder.baseUrl(paymentUrl).build();
    }

    @Override
    public void applyGatewayStatus(GatewayWebhookPayload payload) {
        try {
            restClient.post().uri("/api/payments/internal/transactions/{transactionId}/status", payload.transactionId())
                    .body(new PaymentStatusUpdateRequest(payload.status(), payload.reason()))
                    .retrieve().toBodilessEntity();
        } catch (RestClientException error) {
            throw new WebhookException(502, "Payment service rejected webhook status update");
        }
    }
}
