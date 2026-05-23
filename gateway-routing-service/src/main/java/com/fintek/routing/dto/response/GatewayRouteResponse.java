package com.fintek.routing.dto.response;

import com.fintek.routing.enums.GatewayName;

public record GatewayRouteResponse(GatewayName gateway, String checkoutUrl, boolean fallbackUsed, String reason,
                                   int attempts) {
}
