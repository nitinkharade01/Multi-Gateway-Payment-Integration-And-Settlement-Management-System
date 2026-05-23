package com.fintek.payment.service.impl;

import com.fintek.payment.dto.request.GatewayRouteRequest;
import com.fintek.payment.dto.response.GatewayRouteResponse;
import com.fintek.payment.exception.PaymentException;
import com.fintek.payment.service.GatewayRoutingClient;
import java.time.Duration;
import java.util.concurrent.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class RestGatewayRoutingClient implements GatewayRoutingClient {
    private static final Logger log = LoggerFactory.getLogger(RestGatewayRoutingClient.class);
    private final RestClient restClient;

    public RestGatewayRoutingClient(RestClient.Builder builder, @Value("${services.routing-url}") String routeUrl) {
        this.restClient = builder.baseUrl(routeUrl).build();
    }

    @Override
    public GatewayRouteResponse route(GatewayRouteRequest request) {
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int attempt = 1; attempt <= 3; attempt++) {
                Future<GatewayRouteResponse> call = executor.submit(() -> restClient.post().uri("/api/gateway/route")
                        .body(request).retrieve().body(GatewayRouteResponse.class));
                try {
                    return call.get(Duration.ofSeconds(2).toMillis(), TimeUnit.MILLISECONDS);
                } catch (InterruptedException error) {
                    Thread.currentThread().interrupt();
                    throw new PaymentException(503, "Gateway routing was interrupted");
                } catch (ExecutionException | TimeoutException error) {
                    call.cancel(true);
                    log.warn("Routing attempt {} failed for transaction {}", attempt, request.transactionId());
                }
            }
        }
        throw new PaymentException(503, "No gateway route could be obtained after retries");
    }
}
