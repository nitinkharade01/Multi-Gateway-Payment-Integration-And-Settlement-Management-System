package com.fintek.merchant.service;

import com.fintek.merchant.dto.request.*;
import com.fintek.merchant.entity.MerchantApiKey;
import com.fintek.merchant.enums.*;
import com.fintek.merchant.exception.MerchantException;
import com.fintek.merchant.mapper.MerchantMapper;
import com.fintek.merchant.repository.*;
import com.fintek.merchant.service.impl.MerchantServiceImpl;
import com.fintek.merchant.support.MerchantTestDataBuilder;
import com.fintek.merchant.validator.MerchantValidator;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MerchantServiceImplTest {
    @Mock
    private MerchantRepository merchants;
    @Mock
    private MerchantApiKeyRepository apiKeys;

    private PasswordEncoder passwordEncoder;
    private MerchantServiceImpl service;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        service = new MerchantServiceImpl(merchants, apiKeys, new MerchantValidator(merchants),
                new MerchantMapper(), passwordEncoder);
    }

    @Test
    void shouldRegisterMerchantSuccessfullyWithPendingDefaults() {
        when(merchants.save(any())).thenAnswer(answer -> answer.getArgument(0));

        var response = service.register(MerchantTestDataBuilder.registrationRequest());

        assertEquals(MerchantStatus.PENDING, response.status(), "New merchants should start as PENDING");
        assertEquals(KycStatus.PENDING, response.kycStatus(), "New merchants should start with pending KYC");
        assertEquals("owner@example.test", response.email(), "Merchant email should be normalized");
    }

    @Test
    void shouldRejectMerchantWithDuplicateEmail() {
        when(merchants.existsByEmailIgnoreCase("owner@example.test")).thenReturn(true);

        MerchantException error = assertThrows(MerchantException.class,
                () -> service.register(MerchantTestDataBuilder.registrationRequest()),
                "Duplicate merchant email should be rejected");

        assertEquals(409, error.status(), "Duplicate merchant should map to conflict");
        verify(merchants, never()).save(any());
    }

    @Test
    void shouldUpdateMerchantKycStatusToVerifiedAndActivateMerchant() {
        when(merchants.findById("mrc_1")).thenReturn(Optional.of(MerchantTestDataBuilder.merchant(
                MerchantStatus.PENDING, KycStatus.PENDING)));
        when(merchants.save(any())).thenAnswer(answer -> answer.getArgument(0));

        var response = service.updateStatus("mrc_1", new MerchantStatusRequest(MerchantStatus.ACTIVE, KycStatus.VERIFIED));

        assertEquals(MerchantStatus.ACTIVE, response.status(), "Verified merchant should be activatable");
        assertEquals(KycStatus.VERIFIED, response.kycStatus(), "KYC should move to verified");
    }

    @Test
    void shouldRejectInvalidKycStatusTransition() {
        MerchantException error = assertThrows(MerchantException.class,
                () -> service.updateStatus("mrc_1", new MerchantStatusRequest(MerchantStatus.ACTIVE, KycStatus.PENDING)),
                "ACTIVE status should require verified KYC");

        assertEquals(409, error.status(), "Invalid status transition should map to conflict");
    }

    @Test
    void shouldGenerateApiKeyAndHashSecretBeforeSaving() {
        when(merchants.findById("mrc_1")).thenReturn(Optional.of(MerchantTestDataBuilder.activeMerchant()));
        when(apiKeys.findByMerchantIdAndEnabledTrue("mrc_1")).thenReturn(List.of());

        var response = service.rotateApiKey("mrc_1");

        ArgumentCaptor<MerchantApiKey> captor = ArgumentCaptor.forClass(MerchantApiKey.class);
        verify(apiKeys).save(captor.capture());
        assertTrue(response.apiKey().startsWith("pk_live_"), "API key should use live key prefix");
        assertTrue(response.apiSecret().startsWith("sk_live_"), "API secret should be returned only once");
        assertNotEquals(response.apiSecret(), captor.getValue().getSecretHash(),
                "Plain API secret must not be stored");
        assertTrue(passwordEncoder.matches(response.apiSecret(), captor.getValue().getSecretHash()),
                "Stored API secret hash should verify via BCrypt");
    }

    @Test
    void shouldRejectApiKeyRotationForBlockedMerchant() {
        when(merchants.findById("mrc_1")).thenReturn(Optional.of(MerchantTestDataBuilder.merchant(
                MerchantStatus.BLOCKED, KycStatus.VERIFIED)));

        assertThrows(MerchantException.class, () -> service.rotateApiKey("mrc_1"),
                "Blocked merchants should not be able to rotate credentials");
    }

    @Test
    void shouldValidateActiveApiKeySuccessfully() {
        var merchant = MerchantTestDataBuilder.activeMerchant();
        var apiKey = MerchantTestDataBuilder.apiKey(merchant, passwordEncoder.encode("secret"));
        when(apiKeys.findByApiKey("pk_live_test")).thenReturn(Optional.of(apiKey));

        var response = service.validateCredentials(new CredentialValidationRequest("pk_live_test", "secret"));

        assertTrue(response.valid(), "Active non-expired credentials should validate");
        assertEquals("mrc_1", response.merchantId(), "Credential validation should return owning merchant");
    }

    @Test
    void shouldRejectExpiredDisabledOrBlockedApiKey() {
        var blocked = MerchantTestDataBuilder.merchant(MerchantStatus.BLOCKED, KycStatus.VERIFIED);
        var apiKey = MerchantTestDataBuilder.apiKey(blocked, passwordEncoder.encode("secret"));
        when(apiKeys.findByApiKey("pk_live_test")).thenReturn(Optional.of(apiKey));

        assertThrows(MerchantException.class,
                () -> service.validateCredentials(new CredentialValidationRequest("pk_live_test", "secret")),
                "Blocked merchant credentials should be rejected");

        apiKey.setMerchant(MerchantTestDataBuilder.activeMerchant());
        apiKey.setEnabled(false);
        assertThrows(MerchantException.class,
                () -> service.validateCredentials(new CredentialValidationRequest("pk_live_test", "secret")),
                "Disabled credentials should be rejected");

        apiKey.setEnabled(true);
        apiKey.setExpiresAt(Instant.now().minusSeconds(1));
        assertThrows(MerchantException.class,
                () -> service.validateCredentials(new CredentialValidationRequest("pk_live_test", "secret")),
                "Expired credentials should be rejected");
    }

    @Test
    void shouldUpdateWebhookUrlSuccessfullyAndRejectInvalidWebhookUrl() {
        when(merchants.findById("mrc_1")).thenReturn(Optional.of(MerchantTestDataBuilder.activeMerchant()));
        when(merchants.save(any())).thenAnswer(answer -> answer.getArgument(0));

        var response = service.updateWebhook("mrc_1", new WebhookUrlRequest("https://merchant.test/callback"));

        assertEquals("https://merchant.test/callback", response.webhookUrl(), "Webhook URL should update");
        assertThrows(MerchantException.class, () -> service.updateWebhook("mrc_1", new WebhookUrlRequest("not-a-url")),
                "Invalid webhook URLs should be rejected");
    }
}
