package com.fintek.routing.dto.response;

import com.fintek.routing.enums.GatewayHealth;
import com.fintek.routing.enums.GatewayName;
import java.math.BigDecimal;

public record GatewayHealthResponse(GatewayName gateway, GatewayHealth health, BigDecimal successRate, int priority,
                                    int timeoutMs, int maxRetries) {
}
