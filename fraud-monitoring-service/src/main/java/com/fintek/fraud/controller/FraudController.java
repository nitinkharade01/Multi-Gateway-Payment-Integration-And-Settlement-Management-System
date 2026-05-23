package com.fintek.fraud.controller;

import com.fintek.fraud.dto.request.FraudAssessmentRequest;
import com.fintek.fraud.dto.response.*;
import com.fintek.fraud.service.FraudScoringService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/fraud")
public class FraudController {
    private final FraudScoringService fraud;

    public FraudController(FraudScoringService fraud) {
        this.fraud = fraud;
    }

    @PostMapping("/assess")
    FraudAssessmentResponse assess(@Valid @RequestBody FraudAssessmentRequest request) {
        return fraud.assess(request);
    }

    @GetMapping("/alerts/{merchantId}")
    Page<FraudAlertResponse> alerts(@PathVariable String merchantId, Pageable pageable) {
        return fraud.alerts(merchantId, pageable);
    }
}
