package com.fintek.refund.dto.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record CreateRefundRequest(@NotBlank String merchantId, @NotBlank String transactionId,
                                  @NotNull @Positive @Digits(integer = 16, fraction = 2) BigDecimal amount,
                                  @NotBlank @Size(max = 300) String reason) {
}
