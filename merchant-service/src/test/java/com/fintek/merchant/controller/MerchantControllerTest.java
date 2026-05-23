package com.fintek.merchant.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fintek.merchant.dto.request.MerchantStatusRequest;
import com.fintek.merchant.dto.request.WebhookUrlRequest;
import com.fintek.merchant.dto.response.*;
import com.fintek.merchant.enums.*;
import com.fintek.merchant.exception.MerchantException;
import com.fintek.merchant.service.MerchantService;
import com.fintek.merchant.support.MerchantTestDataBuilder;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MerchantController.class)
@AutoConfigureMockMvc(addFilters = false)
class MerchantControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private MerchantService merchantService;

    @Test
    void shouldRegisterMerchantSuccessfully() throws Exception {
        when(merchantService.register(any())).thenReturn(response());

        mockMvc.perform(post("/api/merchants/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(MerchantTestDataBuilder.registrationRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.merchantId").value("mrc_1"));
    }

    @Test
    void shouldRejectInvalidMerchantRegistrationValidation() throws Exception {
        mockMvc.perform(post("/api/merchants/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"businessName":"","email":"bad","phone":"123","singlePaymentLimit":100}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldGetMerchantUpdateWebhookRotateKeyAndStatus() throws Exception {
        when(merchantService.get("mrc_1")).thenReturn(response());
        when(merchantService.updateWebhook(eq("mrc_1"), any())).thenReturn(response());
        when(merchantService.rotateApiKey("mrc_1")).thenReturn(new ApiKeyResponse("mrc_1", "pk_live_test",
                "sk_live_once", Instant.now().plusSeconds(3600), "store now"));
        when(merchantService.updateStatus(eq("mrc_1"), any())).thenReturn(response());

        mockMvc.perform(get("/api/merchants/mrc_1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("owner@example.test"));

        mockMvc.perform(put("/api/merchants/mrc_1/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new WebhookUrlRequest("https://merchant.test/hook"))))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/merchants/mrc_1/api-key"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.apiSecret").value("sk_live_once"));

        mockMvc.perform(put("/api/merchants/mrc_1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new MerchantStatusRequest(MerchantStatus.ACTIVE, KycStatus.VERIFIED))))
                .andExpect(status().isOk());
    }

    @Test
    void shouldMapServiceExceptionToConflict() throws Exception {
        when(merchantService.register(any())).thenThrow(new MerchantException(409, "duplicate"));

        mockMvc.perform(post("/api/merchants/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(MerchantTestDataBuilder.registrationRequest())))
                .andExpect(status().isConflict());
    }

    private MerchantResponse response() {
        return new MerchantResponse("mrc_1", "Ada Stores", "owner@example.test", "+919876543210",
                MerchantStatus.ACTIVE, KycStatus.VERIFIED, "https://merchant.test/hook",
                new BigDecimal("50000.00"), Instant.parse("2026-05-01T10:15:30Z"));
    }
}
