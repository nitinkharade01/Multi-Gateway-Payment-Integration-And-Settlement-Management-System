package com.fintek.routing.entity;

import com.fintek.routing.enums.GatewayHealth;
import com.fintek.routing.enums.GatewayName;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "gateway_configs", indexes = @Index(name = "ix_gateway_name", columnList = "gateway", unique = true))
public class GatewayConfig {
    @Id
    private String id;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, length = 48)
    private GatewayName gateway;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private GatewayHealth health;
    @Column(nullable = false)
    private int priority;
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal successRate;
    @Column(nullable = false)
    private int timeoutMs;
    @Column(nullable = false)
    private int maxRetries;
    @Column(nullable = false)
    private Instant updatedAt;

    public boolean routable() {
        return health != GatewayHealth.INACTIVE && successRate.compareTo(BigDecimal.ZERO) > 0;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public GatewayName getGateway() { return gateway; }
    public void setGateway(GatewayName gateway) { this.gateway = gateway; }
    public GatewayHealth getHealth() { return health; }
    public void setHealth(GatewayHealth health) { this.health = health; }
    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }
    public BigDecimal getSuccessRate() { return successRate; }
    public void setSuccessRate(BigDecimal successRate) { this.successRate = successRate; }
    public int getTimeoutMs() { return timeoutMs; }
    public void setTimeoutMs(int timeoutMs) { this.timeoutMs = timeoutMs; }
    public int getMaxRetries() { return maxRetries; }
    public void setMaxRetries(int maxRetries) { this.maxRetries = maxRetries; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
