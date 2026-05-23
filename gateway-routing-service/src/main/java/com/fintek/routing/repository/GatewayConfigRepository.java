package com.fintek.routing.repository;

import com.fintek.routing.entity.GatewayConfig;
import com.fintek.routing.enums.GatewayName;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GatewayConfigRepository extends JpaRepository<GatewayConfig, String> {
    Optional<GatewayConfig> findByGateway(GatewayName gateway);
}
