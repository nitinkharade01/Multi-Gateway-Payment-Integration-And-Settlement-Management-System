package com.fintek.reconciliation.mapper;

import com.fintek.reconciliation.dto.response.ReconciliationResultResponse;
import com.fintek.reconciliation.entity.ReconciliationResult;
import org.springframework.stereotype.Component;

@Component
public class ReconciliationMapper {
    public ReconciliationResultResponse response(ReconciliationResult result) {
        return new ReconciliationResultResponse(result.getRunId(), result.getTransactionId(), result.getInternalAmount(),
                result.getGatewayAmount(), result.getInternalStatus(), result.getGatewayStatus(), result.getStatus(), result.getReason());
    }
}
