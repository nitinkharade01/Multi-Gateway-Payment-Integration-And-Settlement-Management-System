package com.fintek.merchant.mapper;

import com.fintek.merchant.dto.response.MerchantResponse;
import com.fintek.merchant.entity.Merchant;
import org.springframework.stereotype.Component;

@Component
public class MerchantMapper {
    public MerchantResponse response(Merchant merchant) {
        return new MerchantResponse(merchant.getId(), merchant.getBusinessName(), merchant.getEmail(), merchant.getPhone(),
                merchant.getStatus(), merchant.getKycStatus(), merchant.getWebhookUrl(), merchant.getSinglePaymentLimit(),
                merchant.getCreatedAt());
    }
}
