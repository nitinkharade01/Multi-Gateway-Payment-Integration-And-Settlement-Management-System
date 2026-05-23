package com.fintek.merchant.dto.request;

import com.fintek.merchant.enums.KycStatus;
import com.fintek.merchant.enums.MerchantStatus;
import jakarta.validation.constraints.NotNull;

public record MerchantStatusRequest(@NotNull MerchantStatus status, @NotNull KycStatus kycStatus) {
}
