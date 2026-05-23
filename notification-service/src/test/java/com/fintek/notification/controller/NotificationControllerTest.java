package com.fintek.notification.controller;

import com.fintek.notification.dto.response.NotificationResponse;
import com.fintek.notification.enums.*;
import com.fintek.notification.service.NotificationService;
import com.fintek.notification.support.TestDataFactory;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificationController.class)
class NotificationControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private NotificationService notificationService;

    @Test
    void shouldReturnMerchantNotificationLogs() throws Exception {
        when(notificationService.merchantLogs(eq("mrc_1"), any())).thenReturn(new PageImpl<>(List.of(
                new NotificationResponse("evt_success", "mrc_1", NotificationChannel.EMAIL,
                        NotificationStatus.SIMULATED, "merchant-contact:mrc_1", TestDataFactory.TIME))));

        mockMvc.perform(get("/api/notifications/merchant/mrc_1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].eventId").value("evt_success"));
    }
}
