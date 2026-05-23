package com.fintek.notification.controller;

import com.fintek.notification.dto.response.NotificationResponse;
import com.fintek.notification.service.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationService notifications;

    public NotificationController(NotificationService notifications) {
        this.notifications = notifications;
    }

    @GetMapping("/merchant/{merchantId}")
    Page<NotificationResponse> merchant(@PathVariable String merchantId, Pageable pageable) {
        return notifications.merchantLogs(merchantId, pageable);
    }
}
