package com.fintek.reconciliation.service;

import com.fintek.reconciliation.dto.request.RunReconciliationRequest;
import com.fintek.reconciliation.dto.response.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReconciliationService {
    CsvUploadResponse upload(byte[] csv);
    ReconciliationRunResponse run(RunReconciliationRequest request);
    Page<ReconciliationResultResponse> mismatches(Pageable pageable);
}
