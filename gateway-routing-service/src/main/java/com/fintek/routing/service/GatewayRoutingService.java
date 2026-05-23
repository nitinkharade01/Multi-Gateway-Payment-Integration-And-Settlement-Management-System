package com.fintek.routing.service;

import com.fintek.routing.dto.request.GatewayConfigRequest;
import com.fintek.routing.dto.request.GatewayRouteRequest;
import com.fintek.routing.dto.response.GatewayHealthResponse;
import com.fintek.routing.dto.response.GatewayRouteResponse;
import com.fintek.routing.enums.GatewayName;
import java.util.List;

public interface GatewayRoutingService {
    GatewayRouteResponse route(GatewayRouteRequest request);
    List<GatewayHealthResponse> health();
    GatewayHealthResponse configure(GatewayName gateway, GatewayConfigRequest request);
}
