package com.fintek.fraud.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fintek.fraud.dto.response.*;
import com.fintek.fraud.enums.*;
import com.fintek.fraud.service.FraudScoringService;
import com.fintek.fraud.support.TestDataFactory;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;
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

@WebMvcTest(FraudController.class)
class FraudControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private FraudScoringService fraudService;

    @Test
    void shouldAnalyzeFraudRequest() throws Exception {
        when(fraudService.assess(any())).thenReturn(new FraudAssessmentResponse("mrc_1", "txn_1", 85,
                RiskLevel.HIGH, Set.of(FraudRule.HIGH_VALUE_TRANSACTION), "fra_1"));

        mockMvc.perform(post("/api/fraud/assess")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(TestDataFactory.request(new BigDecimal("100000.01"), 0, 0))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.riskLevel").value("HIGH"));
    }

    @Test
    void shouldRejectInvalidFraudRequestValidation() throws Exception {
        mockMvc.perform(post("/api/fraud/assess")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"merchantId":"","transactionId":"txn_1","customerPhone":"bad","amount":0,
                                 "status":"SUCCESS","recentRefundRequests":0,"failedWebhookCallbacks":0}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnFraudAlerts() throws Exception {
        when(fraudService.alerts(eq("mrc_1"), any())).thenReturn(new PageImpl<>(List.of(
                new FraudAlertResponse("fra_1", "mrc_1", "txn_1", 85, RiskLevel.HIGH,
                        "HIGH_VALUE_TRANSACTION", Instant.now()))));

        mockMvc.perform(get("/api/fraud/alerts/mrc_1"))
                .andExpect(status().isOk());
    }
}
