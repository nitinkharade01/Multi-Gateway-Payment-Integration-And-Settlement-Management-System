package com.fintek.merchant.service;

import com.fintek.merchant.dto.request.*;
import com.fintek.merchant.dto.response.*;

public interface MerchantService {
    MerchantResponse register(MerchantRegistrationRequest request);
    MerchantResponse get(String merchantId);
    MerchantResponse updateWebhook(String merchantId, WebhookUrlRequest request);
    MerchantResponse updateStatus(String merchantId, MerchantStatusRequest request);
    ApiKeyResponse rotateApiKey(String merchantId);
    CredentialValidationResponse validateCredentials(CredentialValidationRequest request);
}
