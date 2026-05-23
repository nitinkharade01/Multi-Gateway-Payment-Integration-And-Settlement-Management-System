package com.fintek.reconciliation.dto.response;

import java.util.List;

public record ReconciliationRunResponse(String runId, int matched, int mismatched,
                                        List<ReconciliationResultResponse> results) {
}
