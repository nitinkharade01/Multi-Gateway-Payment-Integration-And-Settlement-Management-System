package com.fintek.merchant.support;

import com.fintek.merchant.dto.request.MerchantRegistrationRequest;
import com.fintek.merchant.entity.Merchant;
import com.fintek.merchant.entity.MerchantApiKey;
import com.fintek.merchant.enums.KycStatus;
import com.fintek.merchant.enums.MerchantStatus;
import java.math.BigDecimal;
import java.time.Instant;

public final class MerchantTestDataBuilder {
    private MerchantTestDataBuilder() {
    }

    public static MerchantRegistrationRequest registrationRequest() {
        return new MerchantRegistrationRequest("Ada Stores", "owner@example.test", "+919876543210",
                new BigDecimal("50000.00"));
    }

    public static Merchant activeMerchant() {
        Merchant merchant = merchant(MerchantStatus.ACTIVE, KycStatus.VERIFIED);
        merchant.setWebhookUrl("https://merchant.test/webhook");
        return merchant;
    }

    public static Merchant merchant(MerchantStatus status, KycStatus kycStatus) {
        Merchant merchant = new Merchant();
        merchant.setId("mrc_1");
        merchant.setBusinessName("Ada Stores");
        merchant.setEmail("owner@example.test");
        merchant.setPhone("+919876543210");
        merchant.setStatus(status);
        merchant.setKycStatus(kycStatus);
        merchant.setSinglePaymentLimit(new BigDecimal("50000.00"));
        merchant.setCreatedAt(Instant.parse("2026-05-01T10:15:30Z"));
        merchant.setUpdatedAt(merchant.getCreatedAt());
        return merchant;
    }

    public static MerchantApiKey apiKey(Merchant merchant, String hash) {
        MerchantApiKey apiKey = new MerchantApiKey();
        apiKey.setId("key_1");
        apiKey.setMerchant(merchant);
        apiKey.setApiKey("pk_live_test");
        apiKey.setSecretHash(hash);
        apiKey.setEnabled(true);
        apiKey.setCreatedAt(Instant.now());
        apiKey.setExpiresAt(Instant.now().plusSeconds(3600));
        return apiKey;
    }
}
