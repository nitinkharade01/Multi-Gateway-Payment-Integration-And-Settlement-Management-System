package com.fintek.webhook.repository;

import com.fintek.webhook.enums.GatewayName;
import com.fintek.webhook.support.WebhookTestDataBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class WebhookEventRepositoryTest {
    @Autowired
    private WebhookEventRepository events;

    @Test
    void shouldDetectDuplicateWebhookEventId() {
        events.saveAndFlush(WebhookTestDataBuilder.event(GatewayName.RAZORPAY, "evt_1"));

        assertTrue(events.existsByGatewayAndGatewayEventId(GatewayName.RAZORPAY, "evt_1"),
                "Existing gateway event ID should be detected");
        assertFalse(events.existsByGatewayAndGatewayEventId(GatewayName.CASHFREE, "evt_1"),
                "Duplicate detection should be scoped to gateway");
    }
}
