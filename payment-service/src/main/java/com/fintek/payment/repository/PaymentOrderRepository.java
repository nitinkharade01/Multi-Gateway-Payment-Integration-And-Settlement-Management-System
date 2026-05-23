package com.fintek.payment.repository;

import com.fintek.payment.entity.PaymentOrder;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentOrderRepository extends JpaRepository<PaymentOrder, String> {
    Optional<PaymentOrder> findByOrderId(String orderId);
    Optional<PaymentOrder> findByMerchantIdAndIdempotencyKey(String merchantId, String idempotencyKey);
    Page<PaymentOrder> findByMerchantId(String merchantId, Pageable pageable);
}
