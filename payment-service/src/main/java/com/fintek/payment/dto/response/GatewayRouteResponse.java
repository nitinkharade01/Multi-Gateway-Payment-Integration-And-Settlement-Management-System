package com.fintek.payment.dto.response;

public record GatewayRouteResponse(String gateway, String checkoutUrl, boolean fallbackUsed, String reason) {
}
