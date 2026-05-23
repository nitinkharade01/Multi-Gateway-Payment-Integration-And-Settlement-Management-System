package com.fintek.reconciliation.service.impl;

import com.fintek.reconciliation.dto.request.RunReconciliationRequest;
import com.fintek.reconciliation.dto.response.*;
import com.fintek.reconciliation.entity.ReconciliationResult;
import com.fintek.reconciliation.enums.ReconciliationStatus;
import com.fintek.reconciliation.mapper.ReconciliationMapper;
import com.fintek.reconciliation.repository.ReconciliationResultRepository;
import com.fintek.reconciliation.service.*;
import com.fintek.reconciliation.util.GatewayCsvParser;
import com.fintek.reconciliation.validator.ReconciliationValidator;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReconciliationServiceImpl implements ReconciliationService {
    private final GatewayCsvParser parser;
    private final CsvUploadStore uploads;
    private final InternalTransactionSource internalTransactions;
    private final ReconciliationEngine engine;
    private final ReconciliationResultRepository results;
    private final ReconciliationMapper mapper;
    private final ReconciliationValidator validator;

    public ReconciliationServiceImpl(GatewayCsvParser parser, CsvUploadStore uploads, InternalTransactionSource internalTransactions,
                                     ReconciliationEngine engine, ReconciliationResultRepository results,
                                     ReconciliationMapper mapper, ReconciliationValidator validator) {
        this.parser = parser;
        this.uploads = uploads;
        this.internalTransactions = internalTransactions;
        this.engine = engine;
        this.results = results;
        this.mapper = mapper;
        this.validator = validator;
    }

    @Override
    public CsvUploadResponse upload(byte[] csv) {
        List<GatewaySettlementRecord> records = parser.parse(csv);
        String uploadId = uploads.store(records);
        return new CsvUploadResponse(uploadId, records.size(), "Gateway CSV parsed and staged for reconciliation");
    }

    @Override
    @Transactional
    public ReconciliationRunResponse run(RunReconciliationRequest request) {
        validator.validate(request);
        String runId = "rec_" + UUID.randomUUID().toString().replace("-", "");
        List<ReconciliationResult> compared = engine.compare(runId, request.merchantId(),
                internalTransactions.successfulTransactions(request.merchantId(), request.from(), request.to()),
                uploads.get(request.uploadId()));
        results.saveAll(compared);
        int matched = (int) compared.stream().filter(result -> result.getStatus() == ReconciliationStatus.MATCHED).count();
        return new ReconciliationRunResponse(runId, matched, compared.size() - matched,
                compared.stream().map(mapper::response).toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReconciliationResultResponse> mismatches(Pageable pageable) {
        return results.findByStatusNot(ReconciliationStatus.MATCHED, pageable).map(mapper::response);
    }
}
