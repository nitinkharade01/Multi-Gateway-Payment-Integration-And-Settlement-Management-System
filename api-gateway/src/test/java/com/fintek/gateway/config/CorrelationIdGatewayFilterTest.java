package com.fintek.gateway.config;

import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.*;

class CorrelationIdGatewayFilterTest {
    private final CorrelationIdGatewayFilter filter = new CorrelationIdGatewayFilter();

    @Test
    void shouldApplyCorrelationIdFilter() {
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/auth/login")
                .header("X-Correlation-ID", "corr-123"));

        filter.filter(exchange, chain()).block();

        assertEquals("corr-123", exchange.getResponse().getHeaders().getFirst("X-Correlation-ID"),
                "Gateway should echo an existing correlation ID");
    }

    @Test
    void shouldGenerateCorrelationIdWhenMissing() {
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/payments/orders"));

        filter.filter(exchange, chain()).block();

        assertNotNull(exchange.getResponse().getHeaders().getFirst("X-Correlation-ID"),
                "Gateway should generate missing correlation IDs");
    }

    private GatewayFilterChain chain() {
        return exchange -> {
            assertTrue(exchange.getRequest().getHeaders().containsKey("X-Correlation-ID"),
                    "Downstream exchange should include correlation ID request header");
            return Mono.empty();
        };
    }
}
