package com.fintek.settlement.dto.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;

public record GenerateSettlementRequest(@NotBlank String merchantId, @NotNull Instant from, @NotNull Instant to,
                                        @NotNull @DecimalMin("0.00") @DecimalMax("10.00") BigDecimal gatewayChargePercentage,
                                        @NotNull @DecimalMin("0.00") @DecimalMax("10.00") BigDecimal platformFeePercentage) {
}
