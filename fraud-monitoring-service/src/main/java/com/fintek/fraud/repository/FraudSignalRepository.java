package com.fintek.fraud.repository;

import com.fintek.fraud.entity.FraudSignal;
import java.time.Instant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FraudSignalRepository extends JpaRepository<FraudSignal, String> {
    long countByMerchantIdAndObservedAtAfter(String merchantId, Instant after);
    long countByMerchantIdAndStatusAndObservedAtAfter(String merchantId, String status, Instant after);
    long countByCustomerPhoneAndObservedAtAfter(String customerPhone, Instant after);
}
