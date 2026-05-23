package com.fintek.reconciliation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public record RunReconciliationRequest(@NotBlank String uploadId, @NotBlank String merchantId,
                                       @NotNull Instant from, @NotNull Instant to) {
}
