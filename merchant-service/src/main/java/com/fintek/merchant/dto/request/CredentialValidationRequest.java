package com.fintek.merchant.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CredentialValidationRequest(@NotBlank String apiKey, @NotBlank String apiSecret) {
}
