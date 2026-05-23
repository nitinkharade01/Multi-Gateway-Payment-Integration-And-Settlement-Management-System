package com.fintek.notification.util;

public final class NotificationTemplates {
    public static final String EMAIL = """
            Subject: Payment platform update

            Merchant: %s
            Event: %s
            Detail: %s
            """;
    public static final String WEBHOOK_JSON = """
            {
              "eventId": "%s",
              "merchantId": "%s",
              "type": "%s",
              "detail": "%s"
            }
            """;

    private NotificationTemplates() {
    }
}
