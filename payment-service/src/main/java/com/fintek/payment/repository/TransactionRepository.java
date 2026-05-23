package com.fintek.payment.repository;

import com.fintek.payment.entity.TransactionEntity;
import com.fintek.payment.enums.PaymentStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<TransactionEntity, String> {
    Optional<TransactionEntity> findByTransactionId(String transactionId);
    Optional<TransactionEntity> findFirstByOrderOrderIdOrderByCreatedAtDesc(String orderId);
    List<TransactionEntity> findByMerchantIdAndCreatedAtBetweenAndStatus(String merchantId, Instant from, Instant to, PaymentStatus status);
}
