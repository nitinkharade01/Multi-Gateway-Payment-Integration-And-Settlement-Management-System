package com.fintek.settlement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fintek.settlement.dto.response.SettlementResponse;
import com.fintek.settlement.enums.SettlementStatus;
import com.fintek.settlement.exception.SettlementException;
import com.fintek.settlement.service.SettlementService;
import com.fintek.settlement.support.SettlementTestDataBuilder;
import java.math.BigDecimal;
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

@WebMvcTest(SettlementController.class)
class SettlementControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private SettlementService settlementService;

    @Test
    void shouldGenerateSettlementBatchSuccessfully() throws Exception {
        when(settlementService.generate(any())).thenReturn(response());

        mockMvc.perform(post("/api/settlements/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(SettlementTestDataBuilder.request())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.settlementId").value("set_1"));
    }

    @Test
    void shouldReturnSettlementsByMerchantAndSettlementId() throws Exception {
        when(settlementService.merchant(eq("mrc_1"), any())).thenReturn(new PageImpl<>(List.of(response())));
        when(settlementService.get("set_1")).thenReturn(response());

        mockMvc.perform(get("/api/settlements/merchant/mrc_1"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/settlements/set_1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("GENERATED"));
    }

    @Test
    void shouldRejectInvalidDateRange() throws Exception {
        when(settlementService.generate(any())).thenThrow(new SettlementException(400, "bad range"));

        mockMvc.perform(post("/api/settlements/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(SettlementTestDataBuilder.request())))
                .andExpect(status().isBadRequest());
    }

    private SettlementResponse response() {
        return new SettlementResponse("set_1", "mrc_1", SettlementTestDataBuilder.FROM, SettlementTestDataBuilder.TO,
                1, new BigDecimal("100.00"), new BigDecimal("2.00"), new BigDecimal("1.00"),
                new BigDecimal("0.18"), new BigDecimal("96.82"), SettlementStatus.GENERATED, "csv");
    }
}
