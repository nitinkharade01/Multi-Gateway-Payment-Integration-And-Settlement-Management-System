package com.fintek.webhook.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

public record GatewayWebhookPayload(@JsonProperty("event_id") String eventId,
                                    @JsonProperty("transaction_id") String transactionId,
                                    @JsonProperty("order_id") String orderId,
                                    @JsonProperty("merchant_id") String merchantId,
                                    BigDecimal amount, String currency, String status,
                                    @JsonProperty("merchant_webhook_url") String merchantWebhookUrl,
                                    String reason) {
}
