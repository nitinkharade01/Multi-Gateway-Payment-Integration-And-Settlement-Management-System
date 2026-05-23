package com.fintek.reconciliation.service;

import com.fintek.reconciliation.dto.response.*;
import com.fintek.reconciliation.enums.ReconciliationStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ReconciliationEngineTest {
    private final ReconciliationEngine engine = new ReconciliationEngine();

    @Test
    void reconciliationAmountMismatchIsDetected() {
        var result = engine.compare("run", "mrc", List.of(internal("txn_1", "100.00", "SUCCESS")),
                List.of(new GatewaySettlementRecord("txn_1", new BigDecimal("99.00"), "PAID", "gw"))).getFirst();

        assertEquals(ReconciliationStatus.AMOUNT_MISMATCH, result.getStatus());
        assertTrue(result.getReason().contains("amount"));
    }

    @Test
    void reconciliationStatusMismatchIsDetected() {
        var result = engine.compare("run", "mrc", List.of(internal("txn_2", "100.00", "SUCCESS")),
                List.of(new GatewaySettlementRecord("txn_2", new BigDecimal("100.00"), "FAILED", "gw"))).getFirst();

        assertEquals(ReconciliationStatus.STATUS_MISMATCH, result.getStatus());
    }

    private InternalTransactionRecord internal(String id, String amount, String status) {
        return new InternalTransactionRecord(id, "ord", "mrc", new BigDecimal(amount), "INR", status,
                "PAYU_SIMULATOR", Instant.now());
    }
}
