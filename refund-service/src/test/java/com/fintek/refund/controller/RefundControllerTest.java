package com.fintek.refund.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fintek.refund.dto.response.RefundResponse;
import com.fintek.refund.enums.RefundStatus;
import com.fintek.refund.exception.RefundException;
import com.fintek.refund.service.RefundService;
import com.fintek.refund.support.RefundTestDataBuilder;
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

@WebMvcTest(RefundController.class)
class RefundControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private RefundService refundService;

    @Test
    void shouldInitiateRefundSuccessfully() throws Exception {
        when(refundService.create(any())).thenReturn(response());

        mockMvc.perform(post("/api/refunds")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(RefundTestDataBuilder.refundRequest(new BigDecimal("10.00")))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.refundId").value("rfnd_1"));
    }

    @Test
    void shouldRejectInvalidRefundAmount() throws Exception {
        mockMvc.perform(post("/api/refunds")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"merchantId":"mrc_1","transactionId":"txn_1","amount":0,"reason":"bad"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnRefundByIdAndMerchantRefunds() throws Exception {
        when(refundService.get("rfnd_1")).thenReturn(response());
        when(refundService.merchantRefunds(eq("mrc_1"), any())).thenReturn(new PageImpl<>(List.of(response())));

        mockMvc.perform(get("/api/refunds/rfnd_1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REFUND_SUCCESS"));

        mockMvc.perform(get("/api/refunds/merchant/mrc_1"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldMapRefundServiceErrors() throws Exception {
        when(refundService.get("missing")).thenThrow(new RefundException(404, "not found"));

        mockMvc.perform(get("/api/refunds/missing"))
                .andExpect(status().isNotFound());
    }

    private RefundResponse response() {
        return new RefundResponse("rfnd_1", "mrc_1", "txn_1", new BigDecimal("10.00"),
                new BigDecimal("90.00"), RefundStatus.REFUND_SUCCESS, "gwr_1", Instant.now());
    }
}
