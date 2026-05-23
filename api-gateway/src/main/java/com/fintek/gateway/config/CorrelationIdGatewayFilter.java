package com.fintek.gateway.config;

import java.util.Optional;
import java.util.UUID;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class CorrelationIdGatewayFilter implements GlobalFilter, Ordered {
    private static final String HEADER = "X-Correlation-ID";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String value = Optional.ofNullable(exchange.getRequest().getHeaders().getFirst(HEADER))
                .filter(candidate -> !candidate.isBlank() && candidate.length() <= 128)
                .orElseGet(() -> UUID.randomUUID().toString());
        ServerWebExchange correlated = exchange.mutate()
                .request(exchange.getRequest().mutate().header(HEADER, value).build())
                .response(exchange.getResponse())
                .build();
        correlated.getResponse().getHeaders().set(HEADER, value);
        return chain.filter(correlated);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
