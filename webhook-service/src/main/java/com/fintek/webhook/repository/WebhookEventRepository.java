package com.fintek.webhook.repository;

import com.fintek.webhook.entity.WebhookEvent;
import com.fintek.webhook.enums.GatewayName;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WebhookEventRepository extends JpaRepository<WebhookEvent, String> {
    boolean existsByGatewayAndGatewayEventId(GatewayName gateway, String gatewayEventId);
}
