package com.fintek.routing.service.impl;

import com.fintek.routing.dto.request.*;
import com.fintek.routing.dto.response.*;
import com.fintek.routing.entity.GatewayConfig;
import com.fintek.routing.enums.*;
import com.fintek.routing.exception.RoutingException;
import com.fintek.routing.mapper.GatewayConfigMapper;
import com.fintek.routing.repository.GatewayConfigRepository;
import com.fintek.routing.service.GatewayRoutingService;
import com.fintek.routing.util.GatewaySimulator;
import com.fintek.routing.validator.RoutingRequestValidator;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GatewayRoutingServiceImpl implements GatewayRoutingService {
    private static final Logger log = LoggerFactory.getLogger(GatewayRoutingServiceImpl.class);
    private final GatewayConfigRepository configs;
    private final GatewaySimulator simulator;
    private final RoutingRequestValidator validator;
    private final GatewayConfigMapper mapper;
    private final ExecutorService executor;

    public GatewayRoutingServiceImpl(GatewayConfigRepository configs, GatewaySimulator simulator,
                                     RoutingRequestValidator validator, GatewayConfigMapper mapper, ExecutorService executor) {
        this.configs = configs;
        this.simulator = simulator;
        this.validator = validator;
        this.mapper = mapper;
        this.executor = executor;
    }

    @Override
    @Transactional
    public GatewayRouteResponse route(GatewayRouteRequest request) {
        validator.validate(request);
        Map<GatewayName, GatewayConfig> byGateway = loadConfigs();
        SequencedSet<GatewayName> fallbacks = fallbackOrder(request.paymentMode());
        GatewayName preferred = fallbacks.getFirst();
        int attempts = 0;
        List<String> reasons = new ArrayList<>();
        for (GatewayName candidate : fallbacks) {
            GatewayConfig config = byGateway.get(candidate);
            if (config == null || !config.routable()) {
                reasons.add(candidate + " inactive");
                continue;
            }
            for (int retry = 0; retry <= config.getMaxRetries(); retry++) {
                attempts++;
                Future<String> future = executor.submit(() -> simulator.createCheckout(config, request));
                try {
                    String checkout = future.get(config.getTimeoutMs(), TimeUnit.MILLISECONDS);
                    boolean fallback = candidate != preferred;
                    String reason = fallback ? String.join(", ", reasons) : "preferred route healthy";
                    log.info("Routed {} mode {} to {} after {} attempt(s): {}", request.transactionId(),
                            request.paymentMode(), candidate, attempts, reason);
                    return new GatewayRouteResponse(candidate, checkout, fallback, reason, attempts);
                } catch (InterruptedException error) {
                    future.cancel(true);
                    Thread.currentThread().interrupt();
                    throw new RoutingException(503, "Routing was interrupted");
                } catch (ExecutionException | TimeoutException error) {
                    future.cancel(true);
                    reasons.add(candidate + " attempt " + (retry + 1) + " failed");
                    log.warn("Gateway {} failed checkout attempt {} for {}", candidate, retry + 1, request.transactionId());
                }
            }
        }
        throw new RoutingException(503, "No active gateway accepted route: " + String.join(", ", reasons));
    }

    @Override
    @Transactional(readOnly = true)
    public List<GatewayHealthResponse> health() {
        return loadConfigs().values().stream()
                .sorted(Comparator.comparing(GatewayConfig::getPriority))
                .map(mapper::health).toList();
    }

    @Override
    @Transactional
    public GatewayHealthResponse configure(GatewayName gateway, GatewayConfigRequest request) {
        GatewayConfig config = configs.findByGateway(gateway).orElseGet(() -> defaultConfig(gateway));
        config.setHealth(request.health());
        config.setPriority(request.priority());
        config.setSuccessRate(request.successRate());
        config.setTimeoutMs(request.timeoutMs());
        config.setMaxRetries(request.maxRetries());
        config.setUpdatedAt(Instant.now());
        log.info("Updated gateway {} health {} success-rate {}", gateway, request.health(), request.successRate());
        return mapper.health(configs.save(config));
    }

    public SequencedSet<GatewayName> fallbackOrder(PaymentMode mode) {
        return switch (mode) {
            case UPI, QR -> new LinkedHashSet<>(List.of(GatewayName.CASHFREE_SIMULATOR,
                    GatewayName.RAZORPAY_SIMULATOR, GatewayName.PAYU_SIMULATOR));
            case CARD, WALLET -> new LinkedHashSet<>(List.of(GatewayName.RAZORPAY_SIMULATOR,
                    GatewayName.CASHFREE_SIMULATOR, GatewayName.PAYU_SIMULATOR));
            case NETBANKING -> new LinkedHashSet<>(List.of(GatewayName.PAYU_SIMULATOR,
                    GatewayName.RAZORPAY_SIMULATOR, GatewayName.CASHFREE_SIMULATOR));
        };
    }

    private Map<GatewayName, GatewayConfig> loadConfigs() {
        Map<GatewayName, GatewayConfig> found = new EnumMap<>(GatewayName.class);
        configs.findAll().forEach(config -> found.put(config.getGateway(), config));
        for (GatewayName gateway : GatewayName.values()) {
            found.putIfAbsent(gateway, defaultConfig(gateway));
        }
        return found;
    }

    private GatewayConfig defaultConfig(GatewayName gateway) {
        GatewayConfig config = new GatewayConfig();
        config.setId(gateway.name());
        config.setGateway(gateway);
        config.setHealth(GatewayHealth.ACTIVE);
        config.setPriority(switch (gateway) {
            case RAZORPAY_SIMULATOR -> 1;
            case CASHFREE_SIMULATOR -> 2;
            case PAYU_SIMULATOR -> 3;
        });
        config.setSuccessRate(new BigDecimal("99.00"));
        config.setTimeoutMs(750);
        config.setMaxRetries(1);
        config.setUpdatedAt(Instant.now());
        return config;
    }
}
