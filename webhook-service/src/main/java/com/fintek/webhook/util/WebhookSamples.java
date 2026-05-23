package com.fintek.webhook.util;

public final class WebhookSamples {
    public static final String PAYMENT_SUCCESS = """
            {
              "event_id": "evt_rzp_10001",
              "transaction_id": "txn_6f6a",
              "order_id": "ord_9b11",
              "merchant_id": "mrc_demo",
              "amount": 1250.00,
              "currency": "INR",
              "status": "SUCCESS",
              "merchant_webhook_url": "https://merchant.example/webhooks/payments"
            }
            """;

    private WebhookSamples() {
    }
}
