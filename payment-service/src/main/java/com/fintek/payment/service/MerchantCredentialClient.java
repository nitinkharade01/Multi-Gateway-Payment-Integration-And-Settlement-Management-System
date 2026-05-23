package com.fintek.payment.service;

import com.fintek.payment.dto.response.MerchantCredentialResponse;

public interface MerchantCredentialClient {
    MerchantCredentialResponse validate(String apiKey, String apiSecret);
}
