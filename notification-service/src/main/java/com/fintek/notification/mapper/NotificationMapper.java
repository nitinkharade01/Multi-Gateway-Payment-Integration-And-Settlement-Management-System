package com.fintek.notification.mapper;

import com.fintek.notification.dto.response.NotificationResponse;
import com.fintek.notification.entity.NotificationLog;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {
    public NotificationResponse response(NotificationLog log) {
        return new NotificationResponse(log.getEventId(), log.getMerchantId(), log.getChannel(), log.getStatus(),
                log.getDestination(), log.getCreatedAt());
    }
}
