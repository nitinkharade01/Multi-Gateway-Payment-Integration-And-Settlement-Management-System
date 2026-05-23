package com.fintek.merchant.service.impl;

import com.fintek.common.money.Money;
import com.fintek.merchant.dto.request.*;
import com.fintek.merchant.dto.response.*;
import com.fintek.merchant.entity.Merchant;
import com.fintek.merchant.entity.MerchantApiKey;
import com.fintek.merchant.enums.KycStatus;
import com.fintek.merchant.enums.MerchantStatus;
import com.fintek.merchant.exception.MerchantException;
import com.fintek.merchant.mapper.MerchantMapper;
import com.fintek.merchant.repository.MerchantApiKeyRepository;
import com.fintek.merchant.repository.MerchantRepository;
import com.fintek.merchant.service.MerchantService;
import com.fintek.merchant.util.ApiCredentials;
import com.fintek.merchant.validator.MerchantValidator;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MerchantServiceImpl implements MerchantService {
    private static final Logger log = LoggerFactory.getLogger(MerchantServiceImpl.class);
    private final MerchantRepository merchants;
    private final MerchantApiKeyRepository apiKeys;
    private final MerchantValidator validator;
    private final MerchantMapper mapper;
    private final PasswordEncoder passwordEncoder;

    public MerchantServiceImpl(MerchantRepository merchants, MerchantApiKeyRepository apiKeys,
                               MerchantValidator validator, MerchantMapper mapper, PasswordEncoder passwordEncoder) {
        this.merchants = merchants;
        this.apiKeys = apiKeys;
        this.validator = validator;
        this.mapper = mapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public MerchantResponse register(MerchantRegistrationRequest request) {
        validator.validateRegistration(request);
        Instant now = Instant.now();
        Merchant merchant = new Merchant();
        merchant.setId("mrc_" + UUID.randomUUID());
        merchant.setBusinessName(request.businessName().trim());
        merchant.setEmail(request.email().trim().toLowerCase(Locale.ROOT));
        merchant.setPhone(request.phone().trim());
        merchant.setStatus(MerchantStatus.PENDING);
        merchant.setKycStatus(KycStatus.PENDING);
        merchant.setSinglePaymentLimit(Money.scale(request.singlePaymentLimit() == null
                ? new BigDecimal("500000.00") : request.singlePaymentLimit()));
        merchant.setCreatedAt(now);
        merchant.setUpdatedAt(now);
        log.info("Created pending merchant {} for {}", merchant.getId(), merchant.getEmail());
        return mapper.response(merchants.save(merchant));
    }

    @Override
    @Transactional(readOnly = true)
    public MerchantResponse get(String merchantId) {
        return mapper.response(requireMerchant(merchantId));
    }

    @Override
    @Transactional
    public MerchantResponse updateWebhook(String merchantId, WebhookUrlRequest request) {
        validator.validateWebhook(request.webhookUrl());
        Merchant merchant = requireMerchant(merchantId);
        merchant.setWebhookUrl(request.webhookUrl().trim());
        merchant.setUpdatedAt(Instant.now());
        log.info("Updated webhook URL for merchant {}", merchantId);
        return mapper.response(merchants.save(merchant));
    }

    @Override
    @Transactional
    public MerchantResponse updateStatus(String merchantId, MerchantStatusRequest request) {
        validator.validateStatusTransition(request.status(), request.kycStatus());
        Merchant merchant = requireMerchant(merchantId);
        merchant.setStatus(request.status());
        merchant.setKycStatus(request.kycStatus());
        merchant.setUpdatedAt(Instant.now());
        log.info("Merchant {} moved to status {} and KYC {}", merchantId, request.status(), request.kycStatus());
        return mapper.response(merchants.save(merchant));
    }

    @Override
    @Transactional
    public ApiKeyResponse rotateApiKey(String merchantId) {
        Merchant merchant = requireMerchant(merchantId);
        if (merchant.getStatus() == MerchantStatus.BLOCKED) {
            throw new MerchantException(409, "Blocked merchant cannot rotate API credentials");
        }
        Instant now = Instant.now();
        apiKeys.findByMerchantIdAndEnabledTrue(merchantId).forEach(key -> {
            key.setEnabled(false);
            key.setDisabledAt(now);
        });
        String rawSecret = ApiCredentials.apiSecret();
        MerchantApiKey apiKey = new MerchantApiKey();
        apiKey.setId(UUID.randomUUID().toString());
        apiKey.setMerchant(merchant);
        apiKey.setApiKey(ApiCredentials.apiKey());
        apiKey.setSecretHash(passwordEncoder.encode(rawSecret));
        apiKey.setEnabled(true);
        apiKey.setCreatedAt(now);
        apiKey.setExpiresAt(now.plus(365, ChronoUnit.DAYS));
        apiKeys.save(apiKey);
        log.info("Rotated API key for merchant {}", merchantId);
        return new ApiKeyResponse(merchantId, apiKey.getApiKey(), rawSecret, apiKey.getExpiresAt(),
                "Store this secret now; only the BCrypt hash is retained.");
    }

    @Override
    @Transactional(readOnly = true)
    public CredentialValidationResponse validateCredentials(CredentialValidationRequest request) {
        MerchantApiKey apiKey = apiKeys.findByApiKey(request.apiKey())
                .orElseThrow(() -> new MerchantException(401, "Merchant API key is unknown"));
        Merchant merchant = apiKey.getMerchant();
        if (!apiKey.usableAt(Instant.now())) {
            throw new MerchantException(401, "Merchant API key is expired or disabled");
        }
        if (!passwordEncoder.matches(request.apiSecret(), apiKey.getSecretHash())) {
            throw new MerchantException(401, "Merchant API secret is invalid");
        }
        if (merchant.getStatus() != MerchantStatus.ACTIVE) {
            throw new MerchantException(403, "Merchant must be ACTIVE before accepting payments");
        }
        return new CredentialValidationResponse(true, merchant.getId(), merchant.getStatus().name(),
                merchant.getWebhookUrl(), merchant.getSinglePaymentLimit(), "credential accepted");
    }

    private Merchant requireMerchant(String merchantId) {
        return merchants.findById(merchantId).orElseThrow(() -> new MerchantException(404, "Merchant not found"));
    }
}
