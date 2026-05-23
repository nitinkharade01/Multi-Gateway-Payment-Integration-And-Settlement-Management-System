package com.fintek.merchant.controller;

import com.fintek.merchant.dto.request.*;
import com.fintek.merchant.dto.response.*;
import com.fintek.merchant.service.MerchantService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/merchants")
public class MerchantController {
    private final MerchantService merchants;

    public MerchantController(MerchantService merchants) {
        this.merchants = merchants;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    MerchantResponse register(@Valid @RequestBody MerchantRegistrationRequest request) {
        return merchants.register(request);
    }

    @GetMapping("/{merchantId}")
    MerchantResponse get(@PathVariable String merchantId) {
        return merchants.get(merchantId);
    }

    @PutMapping("/{merchantId}/webhook")
    MerchantResponse webhook(@PathVariable String merchantId, @Valid @RequestBody WebhookUrlRequest request) {
        return merchants.updateWebhook(merchantId, request);
    }

    @PostMapping("/{merchantId}/api-key")
    ApiKeyResponse rotateKey(@PathVariable String merchantId) {
        return merchants.rotateApiKey(merchantId);
    }

    @PutMapping("/{merchantId}/status")
    MerchantResponse status(@PathVariable String merchantId, @Valid @RequestBody MerchantStatusRequest request) {
        return merchants.updateStatus(merchantId, request);
    }

    @PostMapping("/credentials/validate")
    CredentialValidationResponse credentials(@Valid @RequestBody CredentialValidationRequest request) {
        return merchants.validateCredentials(request);
    }
}
