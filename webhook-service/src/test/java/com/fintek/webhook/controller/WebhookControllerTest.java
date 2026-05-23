package com.fintek.webhook.controller;

import com.fintek.webhook.dto.response.WebhookResponse;
import com.fintek.webhook.enums.*;
import com.fintek.webhook.exception.WebhookException;
import com.fintek.webhook.service.WebhookProcessingService;
import com.fintek.webhook.support.WebhookTestDataBuilder;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WebhookController.class)
class WebhookControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private WebhookProcessingService webhookService;

    @Test
    void shouldAcceptGatewayWebhookEndpoints() throws Exception {
        when(webhookService.process(any(), anyString(), anyString(), anyString())).thenAnswer(answer ->
                new WebhookResponse(answer.getArgument(0), "evt_1", "txn_1", WebhookProcessingStatus.ACCEPTED, "ok"));
        String timestamp = String.valueOf(Instant.now().getEpochSecond());

        mockMvc.perform(post("/api/webhooks/razorpay")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Gateway-Signature", "sig")
                        .header("X-Gateway-Timestamp", timestamp)
                        .content(WebhookTestDataBuilder.successfulPayload()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gateway").value("RAZORPAY"));

        mockMvc.perform(post("/api/webhooks/cashfree")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Gateway-Signature", "sig")
                        .header("X-Gateway-Timestamp", timestamp)
                        .content(WebhookTestDataBuilder.successfulPayload()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gateway").value("CASHFREE"));

        mockMvc.perform(post("/api/webhooks/payu")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Gateway-Signature", "sig")
                        .header("X-Gateway-Timestamp", timestamp)
                        .content(WebhookTestDataBuilder.successfulPayload()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gateway").value("PAYU"));
    }

    @Test
    void shouldRejectInvalidSignatureAndMissingSignatureHeader() throws Exception {
        when(webhookService.process(any(), anyString(), eq("bad"), anyString()))
                .thenThrow(new WebhookException(401, "bad signature"));

        mockMvc.perform(post("/api/webhooks/razorpay")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Gateway-Signature", "bad")
                        .header("X-Gateway-Timestamp", String.valueOf(Instant.now().getEpochSecond()))
                        .content(WebhookTestDataBuilder.successfulPayload()))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/webhooks/razorpay")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Gateway-Timestamp", String.valueOf(Instant.now().getEpochSecond()))
                        .content(WebhookTestDataBuilder.successfulPayload()))
                .andExpect(status().isBadRequest());
    }
}
