package com.fintek.merchant.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record WebhookUrlRequest(@NotBlank @Size(max = 500) String webhookUrl) {
}
