package com.fintek.fraud.repository;

import com.fintek.fraud.entity.FraudAlert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FraudAlertRepository extends JpaRepository<FraudAlert, String> {
    Page<FraudAlert> findByMerchantId(String merchantId, Pageable pageable);
}
