package com.fintek.merchant.validator;

import com.fintek.merchant.dto.request.MerchantRegistrationRequest;
import com.fintek.merchant.enums.KycStatus;
import com.fintek.merchant.enums.MerchantStatus;
import com.fintek.merchant.exception.MerchantException;
import com.fintek.merchant.repository.MerchantRepository;
import java.net.URI;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class MerchantValidator {
    private final MerchantRepository merchants;

    public MerchantValidator(MerchantRepository merchants) {
        this.merchants = merchants;
    }

    public void validateRegistration(MerchantRegistrationRequest request) {
        if (merchants.existsByEmailIgnoreCase(request.email().trim())) {
            throw new MerchantException(409, "Merchant email is already onboarded");
        }
    }

    public void validateWebhook(String url) {
        try {
            URI uri = URI.create(url.trim());
            String scheme = uri.getScheme();
            if (scheme == null || !Set.of("https", "http").contains(scheme) || uri.getHost() == null) {
                throw new MerchantException(400, "Webhook URL must be an absolute HTTP URL");
            }
        } catch (IllegalArgumentException error) {
            throw new MerchantException(400, "Webhook URL format is invalid");
        }
    }

    public void validateStatusTransition(MerchantStatus status, KycStatus kycStatus) {
        if (status == MerchantStatus.ACTIVE && kycStatus != KycStatus.VERIFIED) {
            throw new MerchantException(409, "Merchant cannot become ACTIVE until KYC is VERIFIED");
        }
    }
}
