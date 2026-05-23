package com.fintek.notification.service;

import com.fintek.notification.service.impl.PaymentEventListener;
import com.fintek.notification.support.TestDataFactory;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class PaymentEventListenerTest {
    @Test
    void shouldHandleKafkaPaymentEventInput() {
        NotificationService notifications = mock(NotificationService.class);
        PaymentEventListener listener = new PaymentEventListener(notifications);

        listener.onEvent(TestDataFactory.paymentSuccess());

        verify(notifications).handle(TestDataFactory.paymentSuccess());
    }
}
