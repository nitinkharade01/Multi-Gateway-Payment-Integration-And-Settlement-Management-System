package com.fintek.fraud.service;

import com.fintek.fraud.dto.request.FraudAssessmentRequest;
import com.fintek.fraud.dto.response.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FraudScoringService {
    FraudAssessmentResponse assess(FraudAssessmentRequest request);
    Page<FraudAlertResponse> alerts(String merchantId, Pageable pageable);
}
