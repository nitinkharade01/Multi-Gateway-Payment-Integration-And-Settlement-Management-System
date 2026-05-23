package com.fintek.routing.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fintek.routing.dto.request.GatewayConfigRequest;
import com.fintek.routing.dto.response.*;
import com.fintek.routing.enums.*;
import com.fintek.routing.exception.RoutingException;
import com.fintek.routing.service.GatewayRoutingService;
import com.fintek.routing.support.TestDataFactory;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GatewayRoutingController.class)
class GatewayRoutingControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private GatewayRoutingService routingService;

    @Test
    void shouldRouteAndReturnGatewayCheckoutUrl() throws Exception {
        when(routingService.route(any())).thenReturn(new GatewayRouteResponse(GatewayName.CASHFREE_SIMULATOR,
                "https://checkout.local/cashfree/txn_1", false, "preferred", 1));

        mockMvc.perform(post("/api/gateway/route")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(TestDataFactory.routeRequest(PaymentMode.UPI))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gateway").value("CASHFREE_SIMULATOR"))
                .andExpect(jsonPath("$.checkoutUrl").value("https://checkout.local/cashfree/txn_1"));
    }

    @Test
    void shouldReturnGatewayHealthListAndUpdateGatewayConfig() throws Exception {
        when(routingService.health()).thenReturn(List.of(new GatewayHealthResponse(GatewayName.RAZORPAY_SIMULATOR,
                GatewayHealth.ACTIVE, new BigDecimal("99.00"), 1, 750, 1)));
        when(routingService.configure(eq(GatewayName.RAZORPAY_SIMULATOR), any())).thenReturn(
                new GatewayHealthResponse(GatewayName.RAZORPAY_SIMULATOR, GatewayHealth.DEGRADED,
                        new BigDecimal("75.00"), 2, 500, 1));

        mockMvc.perform(get("/api/gateway/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].gateway").value("RAZORPAY_SIMULATOR"));

        mockMvc.perform(put("/api/gateway/config/RAZORPAY_SIMULATOR")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new GatewayConfigRequest(GatewayHealth.DEGRADED,
                                2, new BigDecimal("75.00"), 500, 1))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.health").value("DEGRADED"));
    }

    @Test
    void shouldRejectUnsupportedOrUnavailableRoutingRequest() throws Exception {
        when(routingService.route(any())).thenThrow(new RoutingException(503, "No active gateway"));

        mockMvc.perform(post("/api/gateway/route")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(TestDataFactory.routeRequest(PaymentMode.UPI))))
                .andExpect(status().isServiceUnavailable());

        mockMvc.perform(post("/api/gateway/route")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"merchantId":"","orderId":"ord_1","transactionId":"txn_1","amount":99,
                                 "currency":"INR","paymentMode":"UPI"}
                                """))
                .andExpect(status().isBadRequest());
    }
}
