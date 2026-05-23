package com.fintek.notification.service.impl;

import com.fintek.common.events.PaymentEvent;
import com.fintek.notification.service.NotificationService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true", matchIfMissing = true)
public class PaymentEventListener {
    private final NotificationService notifications;

    public PaymentEventListener(NotificationService notifications) {
        this.notifications = notifications;
    }

    @KafkaListener(topics = "payment-events")
    public void onEvent(PaymentEvent event) {
        notifications.handle(event);
    }
}
