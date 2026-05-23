package com.fintek.merchant.repository;

import com.fintek.merchant.support.MerchantTestDataBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class MerchantRepositoryTest {
    @Autowired
    private MerchantRepository merchants;

    @Test
    void shouldFindMerchantByEmailAndCheckExists() {
        merchants.save(MerchantTestDataBuilder.activeMerchant());

        assertTrue(merchants.existsByEmailIgnoreCase("OWNER@example.test"),
                "Repository should detect existing merchant email case-insensitively");
        assertTrue(merchants.findByEmailIgnoreCase("owner@example.test").isPresent(),
                "Repository should find merchant by email");
        assertTrue(merchants.findByEmailIgnoreCase("missing@example.test").isEmpty(),
                "Unknown merchant email should return empty result");
    }
}
