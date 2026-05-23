package com.fintek.routing.controller;

import com.fintek.routing.dto.request.*;
import com.fintek.routing.dto.response.*;
import com.fintek.routing.enums.GatewayName;
import com.fintek.routing.service.GatewayRoutingService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/gateway")
public class GatewayRoutingController {
    private final GatewayRoutingService routing;

    public GatewayRoutingController(GatewayRoutingService routing) {
        this.routing = routing;
    }

    @PostMapping("/route")
    GatewayRouteResponse route(@Valid @RequestBody GatewayRouteRequest request) {
        return routing.route(request);
    }

    @GetMapping("/health")
    List<GatewayHealthResponse> health() {
        return routing.health();
    }

    @PutMapping("/config/{gatewayId}")
    GatewayHealthResponse configure(@PathVariable GatewayName gatewayId, @Valid @RequestBody GatewayConfigRequest request) {
        return routing.configure(gatewayId, request);
    }
}
