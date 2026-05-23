package com.fintek.fraud.validator;

import com.fintek.fraud.dto.request.FraudAssessmentRequest;
import com.fintek.fraud.exception.FraudException;
import org.springframework.stereotype.Component;

@Component
public class FraudRequestValidator {
    public void validate(FraudAssessmentRequest request) {
        if (request.status().length() > 32) {
            throw new FraudException(400, "Transaction status is too long");
        }
    }
}
