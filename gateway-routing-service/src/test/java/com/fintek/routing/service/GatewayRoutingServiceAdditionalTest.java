package com.fintek.routing.service;

import com.fintek.routing.dto.request.GatewayConfigRequest;
import com.fintek.routing.dto.request.GatewayRouteRequest;
import com.fintek.routing.entity.GatewayConfig;
import com.fintek.routing.enums.*;
import com.fintek.routing.exception.RoutingException;
import com.fintek.routing.mapper.GatewayConfigMapper;
import com.fintek.routing.repository.GatewayConfigRepository;
import com.fintek.routing.service.impl.GatewayRoutingServiceImpl;
import com.fintek.routing.support.TestDataFactory;
import com.fintek.routing.util.GatewaySimulator;
import com.fintek.routing.validator.RoutingRequestValidator;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GatewayRoutingServiceAdditionalTest {
    @Mock
    private GatewayConfigRepository configs;
    private ExecutorService executor;
    private GatewayRoutingServiceImpl service;

    @BeforeEach
    void setUp() {
        executor = Executors.newVirtualThreadPerTaskExecutor();
        service = new GatewayRoutingServiceImpl(configs, new GatewaySimulator(), new RoutingRequestValidator(),
                new GatewayConfigMapper(), executor);
    }

    @AfterEach
    void tearDown() {
        executor.close();
    }

    @Test
    void shouldRouteUpiQrAndNetbankingToPreferredGateways() {
        when(configs.findAll()).thenReturn(List.of());

        assertEquals(GatewayName.CASHFREE_SIMULATOR, service.route(TestDataFactory.routeRequest(PaymentMode.UPI)).gateway(),
                "UPI should prefer Cashfree simulator");
        assertEquals(GatewayName.CASHFREE_SIMULATOR, service.route(TestDataFactory.routeRequest(PaymentMode.QR)).gateway(),
                "QR should prefer Cashfree simulator");
        assertEquals(GatewayName.PAYU_SIMULATOR, service.route(TestDataFactory.routeRequest(PaymentMode.NETBANKING)).gateway(),
                "Netbanking should prefer PayU simulator");
    }

    @Test
    void shouldRouteCardAndWalletToRazorpaySimulator() {
        when(configs.findAll()).thenReturn(List.of());

        assertEquals(GatewayName.RAZORPAY_SIMULATOR, service.route(TestDataFactory.routeRequest(PaymentMode.CARD)).gateway(),
                "Cards should prefer Razorpay simulator");
        assertEquals(GatewayName.RAZORPAY_SIMULATOR, service.route(TestDataFactory.routeRequest(PaymentMode.WALLET)).gateway(),
                "Wallet should prefer Razorpay simulator");
    }

    @Test
    void shouldFallbackToNextHealthyGatewayAndRejectWhenNoGatewayAvailable() {
        when(configs.findAll()).thenReturn(List.of(
                TestDataFactory.config(GatewayName.RAZORPAY_SIMULATOR, GatewayHealth.INACTIVE, 1),
                TestDataFactory.config(GatewayName.CASHFREE_SIMULATOR, GatewayHealth.ACTIVE, 2),
                TestDataFactory.config(GatewayName.PAYU_SIMULATOR, GatewayHealth.ACTIVE, 3)));

        var fallback = service.route(TestDataFactory.routeRequest(PaymentMode.CARD));

        assertEquals(GatewayName.CASHFREE_SIMULATOR, fallback.gateway(), "Inactive preferred gateway should be skipped");
        assertTrue(fallback.fallbackUsed(), "Fallback flag should be true when preferred gateway is skipped");

        when(configs.findAll()).thenReturn(List.of(
                TestDataFactory.config(GatewayName.RAZORPAY_SIMULATOR, GatewayHealth.INACTIVE, 1),
                TestDataFactory.config(GatewayName.CASHFREE_SIMULATOR, GatewayHealth.INACTIVE, 2),
                TestDataFactory.config(GatewayName.PAYU_SIMULATOR, GatewayHealth.INACTIVE, 3)));

        assertThrows(RoutingException.class, () -> service.route(TestDataFactory.routeRequest(PaymentMode.CARD)),
                "Routing should fail when all gateways are inactive");
    }

    @Test
    void shouldSimulateGatewayTimeoutAndRetryBeforeFallback() {
        GatewayConfig razorpay = TestDataFactory.config(GatewayName.RAZORPAY_SIMULATOR, GatewayHealth.ACTIVE, 1);
        razorpay.setTimeoutMs(50);
        razorpay.setMaxRetries(1);
        when(configs.findAll()).thenReturn(List.of(razorpay,
                TestDataFactory.config(GatewayName.CASHFREE_SIMULATOR, GatewayHealth.ACTIVE, 2)));
        GatewayRouteRequest request = new GatewayRouteRequest("mrc_1", "ord_1", "timeout-razorpay_simulator",
                new BigDecimal("99.00"), "INR", PaymentMode.CARD);

        var response = service.route(request);

        assertEquals(GatewayName.CASHFREE_SIMULATOR, response.gateway(),
                "Timeout on preferred gateway should fall back to next healthy gateway");
        assertEquals(3, response.attempts(), "Two timed-out retries plus one fallback attempt should be counted");
    }

    @Test
    void shouldUpdateGatewayHealthStatusAndReturnGatewayHealthList() {
        GatewayConfig existing = TestDataFactory.config(GatewayName.PAYU_SIMULATOR, GatewayHealth.ACTIVE, 3);
        when(configs.findByGateway(GatewayName.PAYU_SIMULATOR)).thenReturn(Optional.of(existing));
        when(configs.save(existing)).thenReturn(existing);

        var updated = service.configure(GatewayName.PAYU_SIMULATOR,
                new GatewayConfigRequest(GatewayHealth.DEGRADED, 5, new BigDecimal("75.00"), 500, 1));

        assertEquals(GatewayHealth.DEGRADED, updated.health(), "Gateway health should be updated");
        assertEquals(5, updated.priority(), "Gateway priority should be updated");

        when(configs.findAll()).thenReturn(List.of(existing));
        assertFalse(service.health().isEmpty(), "Gateway health endpoint should return configured gateways");
    }
}
