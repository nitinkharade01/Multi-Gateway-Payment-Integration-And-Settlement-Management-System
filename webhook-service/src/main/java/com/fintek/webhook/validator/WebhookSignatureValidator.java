package com.fintek.webhook.validator;

import com.fintek.webhook.exception.WebhookException;
import com.fintek.webhook.util.HmacSignatures;
import java.time.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class WebhookSignatureValidator {
    private final Duration replayWindow;

    public WebhookSignatureValidator(@Value("${webhook.replay-window-seconds}") long replayWindowSeconds) {
        this.replayWindow = Duration.ofSeconds(replayWindowSeconds);
    }

    public Instant validate(String secret, String rawPayload, String signature, String timestampHeader) {
        long epochSeconds;
        try {
            epochSeconds = Long.parseLong(timestampHeader);
        } catch (RuntimeException error) {
            throw new WebhookException(400, "Webhook timestamp header is invalid");
        }
        Instant timestamp = Instant.ofEpochSecond(epochSeconds);
        if (Duration.between(timestamp, Instant.now()).abs().compareTo(replayWindow) > 0) {
            throw new WebhookException(401, "Webhook timestamp is outside replay window");
        }
        String expected = HmacSignatures.sha256(secret, timestampHeader + "." + rawPayload);
        if (!HmacSignatures.constantTimeEquals(expected, signature)) {
            throw new WebhookException(401, "Webhook HMAC signature is invalid");
        }
        return timestamp;
    }
}
