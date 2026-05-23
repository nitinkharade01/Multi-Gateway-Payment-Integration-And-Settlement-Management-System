package com.fintek.routing.validator;

import com.fintek.routing.dto.request.GatewayRouteRequest;
import com.fintek.routing.exception.RoutingException;
import org.springframework.stereotype.Component;

@Component
public class RoutingRequestValidator {
    public void validate(GatewayRouteRequest request) {
        if (!"INR".equals(request.currency())) {
            throw new RoutingException(400, "Simulator routes currently support INR only");
        }
        if (request.orderId().equals(request.transactionId())) {
            throw new RoutingException(400, "Order ID and transaction ID must be distinct");
        }
    }
}
