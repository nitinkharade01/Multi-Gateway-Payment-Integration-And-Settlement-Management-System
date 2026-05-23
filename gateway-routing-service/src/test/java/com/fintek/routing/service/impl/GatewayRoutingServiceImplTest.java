package com.fintek.routing.service.impl;

import com.fintek.routing.dto.request.GatewayRouteRequest;
import com.fintek.routing.dto.response.GatewayRouteResponse;
import com.fintek.routing.entity.GatewayConfig;
import com.fintek.routing.enums.*;
import com.fintek.routing.mapper.GatewayConfigMapper;
import com.fintek.routing.repository.GatewayConfigRepository;
import com.fintek.routing.util.GatewaySimulator;
import com.fintek.routing.validator.RoutingRequestValidator;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GatewayRoutingServiceImplTest {
    private GatewayConfigRepository configs;
    private ExecutorService executor;
    private GatewayRoutingServiceImpl routing;

    @BeforeEach
    void setUp() {
        configs = mock(GatewayConfigRepository.class);
        executor = Executors.newVirtualThreadPerTaskExecutor();
        routing = new GatewayRoutingServiceImpl(configs, new GatewaySimulator(), new RoutingRequestValidator(),
                new GatewayConfigMapper(), executor);
    }

    @AfterEach
    void tearDown() {
        executor.close();
    }

    @Test
    void gatewayRoutingByPaymentModePrefersRazorpayForCards() {
        when(configs.findAll()).thenReturn(activeConfigs());

        GatewayRouteResponse route = routing.route(request(PaymentMode.CARD));

        assertEquals(GatewayName.RAZORPAY_SIMULATOR, route.gateway());
        assertFalse(route.fallbackUsed());
    }

    @Test
    void fallbackGatewaySelectionSkipsInactivePreferredGateway() {
        List<GatewayConfig> values = activeConfigs();
        values.get(0).setHealth(GatewayHealth.INACTIVE);
        when(configs.findAll()).thenReturn(values);

        GatewayRouteResponse route = routing.route(request(PaymentMode.CARD));

        assertEquals(GatewayName.CASHFREE_SIMULATOR, route.gateway());
        assertTrue(route.fallbackUsed());
        assertTrue(route.reason().contains("inactive"));
    }

    private GatewayRouteRequest request(PaymentMode mode) {
        return new GatewayRouteRequest("mrc_1", "ord_1", "txn_1", new BigDecimal("99.00"), "INR", mode);
    }

    private List<GatewayConfig> activeConfigs() {
        return new ArrayList<>(List.of(config(GatewayName.RAZORPAY_SIMULATOR, 1),
                config(GatewayName.CASHFREE_SIMULATOR, 2), config(GatewayName.PAYU_SIMULATOR, 3)));
    }

    private GatewayConfig config(GatewayName gateway, int priority) {
        GatewayConfig config = new GatewayConfig();
        config.setId(gateway.name());
        config.setGateway(gateway);
        config.setPriority(priority);
        config.setHealth(GatewayHealth.ACTIVE);
        config.setSuccessRate(new BigDecimal("99.0"));
        config.setTimeoutMs(500);
        config.setMaxRetries(0);
        config.setUpdatedAt(Instant.now());
        return config;
    }
}
