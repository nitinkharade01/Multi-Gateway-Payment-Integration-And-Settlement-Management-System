package com.fintek.routing.mapper;

import com.fintek.routing.dto.response.GatewayHealthResponse;
import com.fintek.routing.entity.GatewayConfig;
import org.springframework.stereotype.Component;

@Component
public class GatewayConfigMapper {
    public GatewayHealthResponse health(GatewayConfig config) {
        return new GatewayHealthResponse(config.getGateway(), config.getHealth(), config.getSuccessRate(),
                config.getPriority(), config.getTimeoutMs(), config.getMaxRetries());
    }
}
