package com.fintek.webhook.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HmacSignaturesTest {
    @Test
    void shouldGenerateSameHmacForSamePayloadAndSecret() {
        String first = HmacSignatures.sha256("secret", "payload");
        String second = HmacSignatures.sha256("secret", "payload");

        assertEquals(first, second, "Same secret and payload should produce same HMAC");
        assertTrue(HmacSignatures.constantTimeEquals(first, second), "Equal signatures should compare safely");
    }

    @Test
    void shouldGenerateDifferentHmacForDifferentPayloadAndRejectBadCompare() {
        String first = HmacSignatures.sha256("secret", "payload");
        String second = HmacSignatures.sha256("secret", "payload-2");

        assertNotEquals(first, second, "Different payloads should produce different HMACs");
        assertFalse(HmacSignatures.constantTimeEquals(first, second), "Different signatures should not compare equal");
        assertFalse(HmacSignatures.constantTimeEquals(first, null), "Null supplied signature should not compare equal");
    }
}
