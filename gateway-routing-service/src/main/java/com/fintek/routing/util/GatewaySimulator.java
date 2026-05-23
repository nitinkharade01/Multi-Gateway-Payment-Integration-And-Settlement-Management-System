package com.fintek.routing.util;

import com.fintek.routing.dto.request.GatewayRouteRequest;
import com.fintek.routing.entity.GatewayConfig;
import com.fintek.routing.exception.RoutingException;
import org.springframework.stereotype.Component;

@Component
public class GatewaySimulator {
    public String createCheckout(GatewayConfig config, GatewayRouteRequest request) {
        if (request.transactionId().contains("timeout-" + config.getGateway().name().toLowerCase())) {
            try {
                Thread.sleep(config.getTimeoutMs() * 2L);
            } catch (InterruptedException error) {
                Thread.currentThread().interrupt();
                throw new RoutingException(504, "Gateway simulator interrupted");
            }
        }
        if (request.transactionId().contains("reject-" + config.getGateway().name().toLowerCase())) {
            throw new RoutingException(502, config.getGateway() + " rejected simulated checkout");
        }
        return "https://checkout.local/" + config.getGateway().name().toLowerCase() + "/" + request.transactionId();
    }
}
