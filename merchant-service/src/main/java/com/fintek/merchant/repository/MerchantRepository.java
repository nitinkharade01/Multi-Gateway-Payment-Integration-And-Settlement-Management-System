package com.fintek.merchant.repository;

import com.fintek.merchant.entity.Merchant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MerchantRepository extends JpaRepository<Merchant, String> {
    boolean existsByEmailIgnoreCase(String email);
    Optional<Merchant> findByEmailIgnoreCase(String email);
}
