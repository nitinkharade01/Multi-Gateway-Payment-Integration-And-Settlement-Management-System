package com.fintek.settlement.repository;

import com.fintek.settlement.entity.Settlement;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettlementRepository extends JpaRepository<Settlement, String> {
    boolean existsByMerchantIdAndRangeStartAndRangeEnd(String merchantId, Instant rangeStart, Instant rangeEnd);
    Optional<Settlement> findBySettlementId(String settlementId);
    Page<Settlement> findByMerchantId(String merchantId, Pageable pageable);
}
