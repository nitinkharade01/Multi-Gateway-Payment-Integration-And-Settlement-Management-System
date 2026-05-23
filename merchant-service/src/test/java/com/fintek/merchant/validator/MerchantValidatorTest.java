package com.fintek.merchant.validator;

import com.fintek.merchant.enums.KycStatus;
import com.fintek.merchant.enums.MerchantStatus;
import com.fintek.merchant.exception.MerchantException;
import com.fintek.merchant.repository.MerchantRepository;
import com.fintek.merchant.support.MerchantTestDataBuilder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MerchantValidatorTest {
    private final MerchantRepository merchants = mock(MerchantRepository.class);
    private final MerchantValidator validator = new MerchantValidator(merchants);

    @Test
    void shouldRejectDuplicateEmailAndInvalidWebhookUrl() {
        when(merchants.existsByEmailIgnoreCase("owner@example.test")).thenReturn(true);

        assertThrows(MerchantException.class,
                () -> validator.validateRegistration(MerchantTestDataBuilder.registrationRequest()),
                "Duplicate emails should fail merchant validation");
        assertThrows(MerchantException.class, () -> validator.validateWebhook("ftp://merchant.test/hook"),
                "Webhook URL should be HTTP or HTTPS");
    }

    @Test
    void shouldRejectInvalidKycStatusTransition() {
        assertThrows(MerchantException.class,
                () -> validator.validateStatusTransition(MerchantStatus.ACTIVE, KycStatus.PENDING),
                "ACTIVE status should require verified KYC");
    }
}
