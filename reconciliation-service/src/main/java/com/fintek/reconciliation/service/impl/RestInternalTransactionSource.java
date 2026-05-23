package com.fintek.reconciliation.service.impl;

import com.fintek.reconciliation.dto.response.InternalTransactionRecord;
import com.fintek.reconciliation.exception.ReconciliationException;
import com.fintek.reconciliation.service.InternalTransactionSource;
import java.time.Instant;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class RestInternalTransactionSource implements InternalTransactionSource {
    private final RestClient client;

    public RestInternalTransactionSource(RestClient.Builder builder, @Value("${services.payment-url}") String url) {
        this.client = builder.baseUrl(url).build();
    }

    @Override
    public List<InternalTransactionRecord> successfulTransactions(String merchantId, Instant from, Instant to) {
        try {
            return client.get().uri(uri -> uri.path("/api/payments/internal/transactions")
                            .queryParam("merchantId", merchantId).queryParam("from", from).queryParam("to", to).build())
                    .retrieve().body(new ParameterizedTypeReference<>() {});
        } catch (RestClientException error) {
            throw new ReconciliationException(502, "Unable to load internal transactions");
        }
    }
}
