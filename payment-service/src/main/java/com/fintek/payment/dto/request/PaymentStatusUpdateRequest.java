package com.fintek.payment.dto.request;

import com.fintek.payment.enums.PaymentStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PaymentStatusUpdateRequest(@NotNull PaymentStatus status, @Size(max = 500) String failureReason) {
}
