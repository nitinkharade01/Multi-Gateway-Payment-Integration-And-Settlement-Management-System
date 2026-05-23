package com.fintek.notification.dto.response;

import com.fintek.notification.enums.NotificationChannel;
import com.fintek.notification.enums.NotificationStatus;
import java.time.Instant;

public record NotificationResponse(String eventId, String merchantId, NotificationChannel channel,
                                   NotificationStatus status, String destination, Instant createdAt) {
}
