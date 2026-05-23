package com.fintek.reconciliation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fintek.reconciliation.dto.response.*;
import com.fintek.reconciliation.enums.ReconciliationStatus;
import com.fintek.reconciliation.service.ReconciliationService;
import com.fintek.reconciliation.support.TestDataFactory;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReconciliationController.class)
class ReconciliationControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private ReconciliationService reconciliationService;

    @Test
    void shouldUploadGatewaySettlementCsvSuccessfully() throws Exception {
        when(reconciliationService.upload(any())).thenReturn(new CsvUploadResponse("upl_1", 1, "ok"));
        MockMultipartFile file = new MockMultipartFile("file", "gateway.csv", "text/csv",
                "transaction_id,amount,status\ntxn_1,100.00,PAID\n".getBytes());

        mockMvc.perform(multipart("/api/reconciliation/upload").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uploadId").value("upl_1"));
    }

    @Test
    void shouldRunReconciliationAndReturnMismatches() throws Exception {
        var mismatch = new ReconciliationResultResponse("run_1", "txn_1", new BigDecimal("100.00"),
                new BigDecimal("99.00"), "SUCCESS", "PAID", ReconciliationStatus.AMOUNT_MISMATCH, "amount differs");
        when(reconciliationService.run(any())).thenReturn(new ReconciliationRunResponse("run_1", 0, 1, List.of(mismatch)));
        when(reconciliationService.mismatches(any())).thenReturn(new PageImpl<>(List.of(mismatch)));

        mockMvc.perform(post("/api/reconciliation/run")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(TestDataFactory.runRequest("upl_1"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mismatched").value(1));

        mockMvc.perform(get("/api/reconciliation/mismatches"))
                .andExpect(status().isOk());
    }
}
