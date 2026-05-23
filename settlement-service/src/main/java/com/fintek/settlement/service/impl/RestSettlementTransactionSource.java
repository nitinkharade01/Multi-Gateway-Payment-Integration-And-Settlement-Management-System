package com.fintek.settlement.service.impl;

import com.fintek.settlement.dto.response.SettlementTransactionSnapshot;
import com.fintek.settlement.exception.SettlementException;
import com.fintek.settlement.service.SettlementTransactionSource;
import java.time.Instant;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class RestSettlementTransactionSource implements SettlementTransactionSource {
    private final RestClient restClient;

    public RestSettlementTransactionSource(RestClient.Builder builder, @Value("${services.payment-url}") String paymentUrl) {
        this.restClient = builder.baseUrl(paymentUrl).build();
    }

    @Override
    public List<SettlementTransactionSnapshot> successfulTransactions(String merchantId, Instant from, Instant to) {
        try {
            return restClient.get().uri(uri -> uri.path("/api/payments/internal/transactions")
                            .queryParam("merchantId", merchantId).queryParam("from", from).queryParam("to", to).build())
                    .retrieve().body(new ParameterizedTypeReference<>() {});
        } catch (RestClientException error) {
            throw new SettlementException(502, "Unable to load eligible payment transactions");
        }
    }
}
