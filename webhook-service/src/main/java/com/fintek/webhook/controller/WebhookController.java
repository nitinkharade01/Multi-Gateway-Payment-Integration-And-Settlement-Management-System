package com.fintek.webhook.controller;

import com.fintek.webhook.dto.response.WebhookResponse;
import com.fintek.webhook.enums.GatewayName;
import com.fintek.webhook.service.WebhookProcessingService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/webhooks")
public class WebhookController {
    private final WebhookProcessingService webhooks;

    public WebhookController(WebhookProcessingService webhooks) {
        this.webhooks = webhooks;
    }

    @PostMapping("/razorpay")
    WebhookResponse razorpay(@RequestBody String payload, @RequestHeader("X-Gateway-Signature") String signature,
                             @RequestHeader("X-Gateway-Timestamp") String timestamp) {
        return webhooks.process(GatewayName.RAZORPAY, payload, signature, timestamp);
    }

    @PostMapping("/cashfree")
    WebhookResponse cashfree(@RequestBody String payload, @RequestHeader("X-Gateway-Signature") String signature,
                             @RequestHeader("X-Gateway-Timestamp") String timestamp) {
        return webhooks.process(GatewayName.CASHFREE, payload, signature, timestamp);
    }

    @PostMapping("/payu")
    WebhookResponse payu(@RequestBody String payload, @RequestHeader("X-Gateway-Signature") String signature,
                         @RequestHeader("X-Gateway-Timestamp") String timestamp) {
        return webhooks.process(GatewayName.PAYU, payload, signature, timestamp);
    }
}
