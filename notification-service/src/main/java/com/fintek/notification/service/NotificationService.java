package com.fintek.notification.service;

import com.fintek.common.events.PaymentEvent;
import com.fintek.notification.dto.response.NotificationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationService {
    void handle(PaymentEvent event);
    Page<NotificationResponse> merchantLogs(String merchantId, Pageable pageable);
}
