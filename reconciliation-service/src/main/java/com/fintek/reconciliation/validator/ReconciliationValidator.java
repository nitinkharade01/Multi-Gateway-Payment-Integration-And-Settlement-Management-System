package com.fintek.reconciliation.validator;

import com.fintek.reconciliation.dto.request.RunReconciliationRequest;
import com.fintek.reconciliation.exception.ReconciliationException;
import org.springframework.stereotype.Component;

@Component
public class ReconciliationValidator {
    public void validate(RunReconciliationRequest request) {
        if (!request.to().isAfter(request.from())) {
            throw new ReconciliationException(400, "Reconciliation end must be after start");
        }
    }
}
