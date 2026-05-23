package com.fintek.webhook.dto.request;

public record PaymentStatusUpdateRequest(String status, String failureReason) {
}
