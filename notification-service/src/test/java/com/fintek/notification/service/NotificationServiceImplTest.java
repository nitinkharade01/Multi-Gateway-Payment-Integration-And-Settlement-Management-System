package com.fintek.notification.service;

import com.fintek.notification.entity.NotificationLog;
import com.fintek.notification.enums.*;
import com.fintek.notification.mapper.NotificationMapper;
import com.fintek.notification.repository.NotificationLogRepository;
import com.fintek.notification.service.impl.NotificationServiceImpl;
import com.fintek.notification.support.*;
import com.fintek.notification.util.NotificationTemplates;
import com.fintek.notification.validator.NotificationEventValidator;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {
    @Mock
    private NotificationLogRepository logs;

    private NotificationServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new NotificationServiceImpl(logs, new NotificationMapper(), new NotificationEventValidator(),
                new DirectExecutorService());
    }

    @Test
    void shouldHandlePaymentCreatedEventAndSkipUnconfiguredWebhook() {
        service.handle(TestDataFactory.paymentCreated());

        ArgumentCaptor<NotificationLog> captor = ArgumentCaptor.forClass(NotificationLog.class);
        verify(logs, times(2)).save(captor.capture());
        assertTrue(captor.getAllValues().stream().anyMatch(log -> log.getChannel() == NotificationChannel.EMAIL),
                "Payment created event should render an email notification");
        assertTrue(captor.getAllValues().stream().anyMatch(log -> log.getStatus() == NotificationStatus.SKIPPED),
                "Payment created event without webhook URL should skip merchant callback");
    }

    @Test
    void shouldHandlePaymentSuccessAndFailedEventsWithMerchantWebhookCallbacks() {
        service.handle(TestDataFactory.paymentSuccess());
        service.handle(TestDataFactory.paymentFailed());

        ArgumentCaptor<NotificationLog> captor = ArgumentCaptor.forClass(NotificationLog.class);
        verify(logs, times(4)).save(captor.capture());
        assertEquals(2, captor.getAllValues().stream()
                        .filter(log -> log.getChannel() == NotificationChannel.MERCHANT_WEBHOOK
                                && log.getStatus() == NotificationStatus.SIMULATED).count(),
                "Success and failed events with callback URLs should simulate merchant webhooks");
    }

    @Test
    void shouldHandleRefundSettlementAndFraudEvents() {
        service.handle(TestDataFactory.refundSuccess());
        service.handle(TestDataFactory.settlementGenerated());
        service.handle(TestDataFactory.fraudAlert());

        verify(logs, times(6)).save(any(NotificationLog.class));
    }

    @Test
    void shouldUseTextBlockTemplateForEmail() {
        String message = NotificationTemplates.EMAIL.formatted("mrc_1", "PAYMENT_SUCCESS", "captured");

        assertTrue(message.contains("Subject: Payment platform update"),
                "Email template should include subject from text block");
        assertTrue(message.contains("Merchant: mrc_1"), "Email template should render merchant ID");
    }

    @Test
    void shouldHandleNotificationFailureAndRejectInvalidEvent() {
        var invalid = new com.fintek.common.events.PaymentCreatedEvent("", TestDataFactory.TIME, "mrc_1",
                "ord_1", "txn_1", java.math.BigDecimal.ONE, "INR");

        assertThrows(IllegalArgumentException.class, () -> service.handle(invalid),
                "Events without IDs should be rejected before notification fan-out");
    }

    @Test
    void shouldReturnMerchantNotificationLogs() {
        when(logs.findByMerchantId(eq("mrc_1"), any())).thenReturn(new PageImpl<>(List.of(TestDataFactory.log())));

        var page = service.merchantLogs("mrc_1", PageRequest.of(0, 10));

        assertEquals(1, page.getTotalElements(), "Merchant logs should be returned as response page");
        assertEquals(NotificationChannel.EMAIL, page.getContent().getFirst().channel(), "Notification channel should map");
    }
}
