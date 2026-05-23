package com.fintek.refund.dto.request;

public record PaymentStatusUpdateRequest(String status, String failureReason) {
}
