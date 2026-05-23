package com.fintek.reconciliation.repository;

import com.fintek.reconciliation.entity.ReconciliationResult;
import com.fintek.reconciliation.enums.ReconciliationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReconciliationResultRepository extends JpaRepository<ReconciliationResult, String> {
    Page<ReconciliationResult> findByStatusNot(ReconciliationStatus status, Pageable pageable);
}
