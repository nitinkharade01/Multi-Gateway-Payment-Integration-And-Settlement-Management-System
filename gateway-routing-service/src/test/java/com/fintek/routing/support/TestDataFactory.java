package com.fintek.routing.support;

import com.fintek.routing.dto.request.GatewayRouteRequest;
import com.fintek.routing.entity.GatewayConfig;
import com.fintek.routing.enums.*;
import java.math.BigDecimal;
import java.time.Instant;

public final class TestDataFactory {
    private TestDataFactory() {
    }

    public static GatewayRouteRequest routeRequest(PaymentMode mode) {
        return new GatewayRouteRequest("mrc_1", "ord_1", "txn_1", new BigDecimal("99.00"), "INR", mode);
    }

    public static GatewayConfig config(GatewayName gateway, GatewayHealth health, int priority) {
        GatewayConfig config = new GatewayConfig();
        config.setId(gateway.name());
        config.setGateway(gateway);
        config.setHealth(health);
        config.setPriority(priority);
        config.setSuccessRate(new BigDecimal("99.00"));
        config.setTimeoutMs(250);
        config.setMaxRetries(0);
        config.setUpdatedAt(Instant.parse("2026-05-01T10:15:30Z"));
        return config;
    }
}
