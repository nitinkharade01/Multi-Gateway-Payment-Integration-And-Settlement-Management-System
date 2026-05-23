package com.fintek.reconciliation.repository;

import com.fintek.reconciliation.enums.ReconciliationStatus;
import com.fintek.reconciliation.support.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class ReconciliationResultRepositoryTest {
    @Autowired
    private ReconciliationResultRepository results;

    @Test
    void shouldStoreReconciliationResultsAndReturnMismatches() {
        results.saveAndFlush(TestDataFactory.result("txn_1", ReconciliationStatus.AMOUNT_MISMATCH));
        results.saveAndFlush(TestDataFactory.result("txn_2", ReconciliationStatus.MATCHED));

        var mismatches = results.findByStatusNot(ReconciliationStatus.MATCHED, PageRequest.of(0, 10));

        assertEquals(1, mismatches.getTotalElements(), "Mismatch query should exclude matched reconciliation results");
    }
}
