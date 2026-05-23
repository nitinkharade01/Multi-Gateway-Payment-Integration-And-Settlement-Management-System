package com.fintek.reconciliation.service;

import com.fintek.reconciliation.mapper.ReconciliationMapper;
import com.fintek.reconciliation.repository.ReconciliationResultRepository;
import com.fintek.reconciliation.service.impl.ReconciliationServiceImpl;
import com.fintek.reconciliation.support.TestDataFactory;
import com.fintek.reconciliation.util.GatewayCsvParser;
import com.fintek.reconciliation.validator.ReconciliationValidator;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReconciliationServiceImplTest {
    @Mock
    private InternalTransactionSource internalTransactions;
    @Mock
    private ReconciliationResultRepository results;

    private ReconciliationServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ReconciliationServiceImpl(new GatewayCsvParser(), new CsvUploadStore(), internalTransactions,
                new ReconciliationEngine(), results, new ReconciliationMapper(), new ReconciliationValidator());
    }

    @Test
    void shouldUploadGatewaySettlementCsvSuccessfullyAndRunReconciliation() {
        var upload = service.upload("""
                transaction_id,amount,status,gateway_reference
                txn_1,100.00,PAID,gw_1
                """.getBytes(StandardCharsets.UTF_8));
        when(internalTransactions.successfulTransactions(eq("mrc_1"), any(), any()))
                .thenReturn(List.of(TestDataFactory.internal("txn_1", "100.00", "SUCCESS")));

        var response = service.run(TestDataFactory.runRequest(upload.uploadId()));

        assertEquals(1, upload.records(), "Upload should stage one gateway settlement row");
        assertEquals(1, response.matched(), "Matching internal and gateway records should count as matched");
        assertEquals(0, response.mismatched(), "No mismatches should be reported for exact match");
        verify(results).saveAll(any());
    }

    @Test
    void shouldGenerateMismatchReportAndReturnStoredMismatches() {
        when(results.findByStatusNot(any(), any())).thenReturn(new PageImpl<>(List.of(
                TestDataFactory.result("txn_1", com.fintek.reconciliation.enums.ReconciliationStatus.AMOUNT_MISMATCH))));

        var page = service.mismatches(PageRequest.of(0, 10));

        assertEquals(1, page.getTotalElements(), "Mismatch endpoint should return non-matched results");
        assertEquals("txn_1", page.getContent().getFirst().transactionId(), "Mismatch report should include transaction ID");
    }
}
