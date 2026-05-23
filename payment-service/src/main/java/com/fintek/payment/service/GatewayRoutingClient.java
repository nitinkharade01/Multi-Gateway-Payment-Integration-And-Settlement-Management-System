package com.fintek.payment.service;

import com.fintek.payment.dto.request.GatewayRouteRequest;
import com.fintek.payment.dto.response.GatewayRouteResponse;

public interface GatewayRoutingClient {
    GatewayRouteResponse route(GatewayRouteRequest request);
}
