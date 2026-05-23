package com.fintek.refund.repository;

import com.fintek.refund.entity.RefundRecord;
import com.fintek.refund.enums.RefundStatus;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefundRepository extends JpaRepository<RefundRecord, String> {
    Optional<RefundRecord> findByRefundId(String refundId);
    List<RefundRecord> findByTransactionIdAndStatusIn(String transactionId, Collection<RefundStatus> statuses);
    Page<RefundRecord> findByMerchantId(String merchantId, Pageable pageable);
}
