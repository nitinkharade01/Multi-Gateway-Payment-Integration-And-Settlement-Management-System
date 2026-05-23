package com.fintek.payment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fintek.payment.dto.request.PaymentStatusUpdateRequest;
import com.fintek.payment.dto.response.*;
import com.fintek.payment.enums.*;
import com.fintek.payment.exception.PaymentException;
import com.fintek.payment.service.PaymentOrderService;
import com.fintek.payment.support.PaymentTestDataBuilder;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentController.class)
class PaymentControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private PaymentOrderService paymentService;

    @Test
    void shouldCreatePaymentOrderSuccessfully() throws Exception {
        when(paymentService.create(any())).thenReturn(orderResponse(false));

        mockMvc.perform(post("/api/payments/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(PaymentTestDataBuilder.createOrderRequest("idem-1",
                                new BigDecimal("100.00")))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.checkoutUrl").value("https://checkout.test/txn_1"));
    }

    @Test
    void shouldReturnBadRequestForInvalidAmount() throws Exception {
        mockMvc.perform(post("/api/payments/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"merchantId":"mrc_1","apiKey":"pk","apiSecret":"sk","idempotencyKey":"idem",
                                 "amount":0,"currency":"INR","paymentMode":"UPI","customerEmail":"customer@example.test",
                                 "customerPhone":"+919876543210"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnExistingOrderForDuplicateIdempotencyAndGetOrderStatus() throws Exception {
        when(paymentService.create(any())).thenReturn(orderResponse(true));
        when(paymentService.getOrder("ord_1")).thenReturn(orderResponse(false));
        when(paymentService.status("txn_1")).thenReturn(statusResponse());

        mockMvc.perform(post("/api/payments/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(PaymentTestDataBuilder.createOrderRequest("idem-1",
                                new BigDecimal("100.00")))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idempotentReplay").value(true));

        mockMvc.perform(get("/api/payments/orders/ord_1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value("ord_1"));

        mockMvc.perform(get("/api/payments/status/txn_1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void shouldPayOrderListMerchantOrdersAndExposeInternalTransactionApis() throws Exception {
        when(paymentService.pay("ord_1")).thenReturn(orderResponse(false));
        when(paymentService.merchantOrders(eq("mrc_1"), any())).thenReturn(new PageImpl<>(List.of(orderResponse(false))));
        when(paymentService.transaction("txn_1")).thenReturn(new TransactionSnapshotResponse("txn_1", "ord_1", "mrc_1",
                new BigDecimal("100.00"), "INR", PaymentStatus.SUCCESS, "CASHFREE_SIMULATOR", Instant.now()));
        when(paymentService.successfulTransactions(eq("mrc_1"), any(), any())).thenReturn(List.of());
        when(paymentService.updateStatus(eq("txn_1"), any())).thenReturn(statusResponse());

        mockMvc.perform(post("/api/payments/ord_1/pay"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value("txn_1"));

        mockMvc.perform(get("/api/payments/merchant/mrc_1"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/payments/internal/transactions/txn_1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));

        mockMvc.perform(get("/api/payments/internal/transactions")
                        .param("merchantId", "mrc_1")
                        .param("from", "2026-05-01T00:00:00Z")
                        .param("to", "2026-05-02T00:00:00Z"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/payments/internal/transactions/txn_1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new PaymentStatusUpdateRequest(PaymentStatus.FAILED, "declined"))))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnNotFoundForInvalidOrder() throws Exception {
        when(paymentService.getOrder("missing")).thenThrow(new PaymentException(404, "not found"));

        mockMvc.perform(get("/api/payments/orders/missing"))
                .andExpect(status().isNotFound());
    }

    private PaymentOrderResponse orderResponse(boolean replay) {
        return new PaymentOrderResponse("ord_1", "txn_1", "mrc_1", new BigDecimal("100.00"), "INR",
                PaymentMode.UPI, PaymentStatus.PENDING, "CASHFREE_SIMULATOR", "https://checkout.test/txn_1",
                Instant.now().plusSeconds(900), replay);
    }

    private TransactionStatusResponse statusResponse() {
        return new TransactionStatusResponse("txn_1", "ord_1", "mrc_1", new BigDecimal("100.00"), "INR",
                PaymentStatus.PENDING, "CASHFREE_SIMULATOR", null, Instant.now());
    }
}
