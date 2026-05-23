package com.fintek.notification.repository;

import com.fintek.notification.support.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class NotificationLogRepositoryTest {
    @Autowired
    private NotificationLogRepository logs;

    @Test
    void shouldCreateNotificationLogAndReturnByMerchant() {
        logs.saveAndFlush(TestDataFactory.log());

        assertEquals(1, logs.findByMerchantId("mrc_1", PageRequest.of(0, 10)).getTotalElements(),
                "Merchant notification query should include saved log");
    }
}
