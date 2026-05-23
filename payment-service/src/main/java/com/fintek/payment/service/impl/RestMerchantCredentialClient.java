package com.fintek.payment.service.impl;

import com.fintek.payment.dto.response.MerchantCredentialResponse;
import com.fintek.payment.exception.PaymentException;
import com.fintek.payment.service.MerchantCredentialClient;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class RestMerchantCredentialClient implements MerchantCredentialClient {
    private final RestClient restClient;

    public RestMerchantCredentialClient(RestClient.Builder builder, @Value("${services.merchant-url}") String merchantUrl) {
        this.restClient = builder.baseUrl(merchantUrl).build();
    }

    @Override
    public MerchantCredentialResponse validate(String apiKey, String apiSecret) {
        try {
            return restClient.post().uri("/api/merchants/credentials/validate")
                    .body(Map.of("apiKey", apiKey, "apiSecret", apiSecret))
                    .retrieve().body(MerchantCredentialResponse.class);
        } catch (RestClientException error) {
            throw new PaymentException(401, "Merchant credential validation failed");
        }
    }
}
