package com.fintek.notification.validator;

import com.fintek.common.events.PaymentEvent;
import org.springframework.stereotype.Component;

@Component
public class NotificationEventValidator {
    public void validate(PaymentEvent event) {
        if (event.eventId() == null || event.eventId().isBlank()) {
            throw new IllegalArgumentException("Payment event ID is required");
        }
    }
}
