package com.fintek.routing.dto.request;

import com.fintek.routing.enums.GatewayHealth;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record GatewayConfigRequest(@NotNull GatewayHealth health, @Min(1) @Max(100) int priority,
                                   @NotNull @DecimalMin("0.00") @DecimalMax("100.00") BigDecimal successRate,
                                   @Min(50) @Max(10000) int timeoutMs, @Min(0) @Max(5) int maxRetries) {
}
