package com.fintek.reconciliation.service;

import com.fintek.reconciliation.enums.ReconciliationStatus;
import com.fintek.reconciliation.support.TestDataFactory;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ReconciliationEngineAdditionalTest {
    private final ReconciliationEngine engine = new ReconciliationEngine();

    @Test
    void shouldDetectMatchedTransactionAndMapGatewayStatusToInternalStatus() {
        var result = engine.compare("run", "mrc_1",
                List.of(TestDataFactory.internal("txn_1", "100.0", "SUCCESS")),
                List.of(TestDataFactory.gateway("txn_1", "100.00", "CAPTURED"))).getFirst();

        assertEquals(ReconciliationStatus.MATCHED, result.getStatus(),
                "Captured gateway status should normalize to successful internal status");
    }

    @Test
    void shouldDetectMissingInternalAndMissingGatewayTransactions() {
        var results = engine.compare("run", "mrc_1",
                List.of(TestDataFactory.internal("txn_internal", "100.00", "SUCCESS")),
                List.of(TestDataFactory.gateway("txn_gateway", "100.00", "PAID")));

        assertTrue(results.stream().anyMatch(result -> result.getStatus() == ReconciliationStatus.MISSING_INTERNAL),
                "Gateway-only records should be marked missing internally");
        assertTrue(results.stream().anyMatch(result -> result.getStatus() == ReconciliationStatus.MISSING_GATEWAY),
                "Internal-only transactions should be marked missing from gateway CSV");
    }

    @Test
    void shouldDetectDuplicateGatewayRecordAndGroupDuplicates() {
        var result = engine.compare("run", "mrc_1",
                List.of(TestDataFactory.internal("txn_1", "100.00", "SUCCESS")),
                List.of(TestDataFactory.gateway("txn_1", "100.00", "PAID"),
                        TestDataFactory.gateway("txn_1", "100.00", "PAID"))).getFirst();

        assertEquals(ReconciliationStatus.DUPLICATE_GATEWAY_RECORD, result.getStatus(),
                "Duplicate gateway records should be grouped into one mismatch");
        assertTrue(result.getReason().contains("2 rows"), "Duplicate reason should include duplicate count");
    }
}
