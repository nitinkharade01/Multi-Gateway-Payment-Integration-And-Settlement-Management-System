package com.fintek.webhook.support;

import com.fintek.webhook.entity.WebhookEvent;
import com.fintek.webhook.enums.GatewayName;
import com.fintek.webhook.enums.WebhookProcessingStatus;
import com.fintek.webhook.util.HmacSignatures;
import java.time.Instant;

public final class WebhookTestDataBuilder {
    public static final String SECRET = "webhook-secret";

    private WebhookTestDataBuilder() {
    }

    public static String successfulPayload() {
        return """
                {"event_id":"evt_1","transaction_id":"txn_1","order_id":"ord_1","merchant_id":"mrc_1",
                 "amount":125.00,"currency":"INR","status":"SUCCESS","merchant_webhook_url":"https://merchant.test/hook"}
                """;
    }

    public static String failedPayload() {
        return """
                {"event_id":"evt_failed","transaction_id":"txn_2","order_id":"ord_2","merchant_id":"mrc_1",
                 "amount":125.00,"currency":"INR","status":"FAILED","merchant_webhook_url":"https://merchant.test/hook",
                 "reason":"insufficient funds"}
                """;
    }

    public static String signature(String timestamp, String payload) {
        return HmacSignatures.sha256(SECRET, timestamp + "." + payload);
    }

    public static WebhookEvent event(GatewayName gateway, String eventId) {
        WebhookEvent event = new WebhookEvent();
        event.setId("wh_" + eventId);
        event.setGateway(gateway);
        event.setGatewayEventId(eventId);
        event.setRawPayload(successfulPayload());
        event.setReceivedSignature("sig");
        event.setGatewayTimestamp(Instant.now());
        event.setTransactionId("txn_1");
        event.setStatus(WebhookProcessingStatus.ACCEPTED);
        event.setProcessedAt(Instant.now());
        return event;
    }
}
