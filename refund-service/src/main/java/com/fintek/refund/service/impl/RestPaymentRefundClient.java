package com.fintek.refund.service.impl;

import com.fintek.refund.dto.request.PaymentStatusUpdateRequest;
import com.fintek.refund.dto.response.PaymentTransactionSnapshot;
import com.fintek.refund.enums.PaymentStatus;
import com.fintek.refund.exception.RefundException;
import com.fintek.refund.service.PaymentRefundClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class RestPaymentRefundClient implements PaymentRefundClient {
    private final RestClient restClient;

    public RestPaymentRefundClient(RestClient.Builder builder, @Value("${services.payment-url}") String paymentUrl) {
        restClient = builder.baseUrl(paymentUrl).build();
    }

    @Override
    public PaymentTransactionSnapshot transaction(String transactionId) {
        try {
            return restClient.get().uri("/api/payments/internal/transactions/{id}", transactionId)
                    .retrieve().body(PaymentTransactionSnapshot.class);
        } catch (RestClientException error) {
            throw new RefundException(404, "Paid transaction snapshot was not found");
        }
    }

    @Override
    public void update(String transactionId, PaymentStatus status) {
        restClient.post().uri("/api/payments/internal/transactions/{id}/status", transactionId)
                .body(new PaymentStatusUpdateRequest(status.name(), null)).retrieve().toBodilessEntity();
    }
}
