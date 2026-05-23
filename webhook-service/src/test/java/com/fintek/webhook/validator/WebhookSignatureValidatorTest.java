package com.fintek.webhook.validator;

import com.fintek.webhook.exception.WebhookException;
import com.fintek.webhook.support.WebhookTestDataBuilder;
import java.time.Instant;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WebhookSignatureValidatorTest {
    private final WebhookSignatureValidator validator = new WebhookSignatureValidator(300);

    @Test
    void shouldValidateHmacSha256Signature() {
        String timestamp = String.valueOf(Instant.now().getEpochSecond());

        Instant accepted = validator.validate(WebhookTestDataBuilder.SECRET, WebhookTestDataBuilder.successfulPayload(),
                WebhookTestDataBuilder.signature(timestamp, WebhookTestDataBuilder.successfulPayload()), timestamp);

        assertEquals(Instant.ofEpochSecond(Long.parseLong(timestamp)), accepted,
                "Valid signature should return gateway timestamp");
    }

    @Test
    void shouldRejectInvalidMissingOldAndFutureTimestampSignatures() {
        String now = String.valueOf(Instant.now().getEpochSecond());

        assertThrows(WebhookException.class,
                () -> validator.validate(WebhookTestDataBuilder.SECRET, WebhookTestDataBuilder.successfulPayload(), "bad", now),
                "Invalid signature should be rejected");
        assertThrows(WebhookException.class,
                () -> validator.validate(WebhookTestDataBuilder.SECRET, WebhookTestDataBuilder.successfulPayload(), null, now),
                "Missing signature should be rejected");
        assertThrows(WebhookException.class,
                () -> validator.validate(WebhookTestDataBuilder.SECRET, WebhookTestDataBuilder.successfulPayload(), "bad",
                        String.valueOf(Instant.now().minusSeconds(301).getEpochSecond())),
                "Old timestamps should be rejected as replay attacks");
        assertThrows(WebhookException.class,
                () -> validator.validate(WebhookTestDataBuilder.SECRET, WebhookTestDataBuilder.successfulPayload(), "bad",
                        String.valueOf(Instant.now().plusSeconds(301).getEpochSecond())),
                "Future timestamps outside the replay window should be rejected");
    }
}
