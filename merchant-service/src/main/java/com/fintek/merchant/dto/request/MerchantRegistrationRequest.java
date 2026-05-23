package com.fintek.merchant.dto.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record MerchantRegistrationRequest(
        @NotBlank @Size(max = 200) String businessName,
        @NotBlank @Email @Size(max = 190) String email,
        @NotBlank @Pattern(regexp = "^\\+?[1-9]\\d{9,14}$", message = "phone must be E.164-compatible") String phone,
        @Positive @DecimalMax("10000000.00") BigDecimal singlePaymentLimit) {
}
