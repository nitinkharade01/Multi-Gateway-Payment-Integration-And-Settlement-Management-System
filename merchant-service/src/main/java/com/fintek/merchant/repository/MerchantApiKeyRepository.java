package com.fintek.merchant.repository;

import com.fintek.merchant.entity.MerchantApiKey;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MerchantApiKeyRepository extends JpaRepository<MerchantApiKey, String> {
    Optional<MerchantApiKey> findByApiKey(String apiKey);
    List<MerchantApiKey> findByMerchantIdAndEnabledTrue(String merchantId);
}
