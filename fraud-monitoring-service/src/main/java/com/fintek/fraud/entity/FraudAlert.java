package com.fintek.fraud.entity;

import com.fintek.fraud.enums.RiskLevel;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "fraud_alerts", indexes = @Index(name = "ix_fraud_transaction", columnList = "transactionId"))
public class FraudAlert {
    @Id
    private String id;
    @Column(nullable = false, unique = true, length = 72)
    private String alertId;
    @Column(nullable = false, length = 72)
    private String merchantId;
    @Column(nullable = false, length = 72)
    private String transactionId;
    @Column(nullable = false)
    private int score;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private RiskLevel riskLevel;
    @Column(nullable = false, length = 800)
    private String reasons;
    @Column(nullable = false)
    private Instant createdAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getAlertId() { return alertId; }
    public void setAlertId(String alertId) { this.alertId = alertId; }
    public String getMerchantId() { return merchantId; }
    public void setMerchantId(String merchantId) { this.merchantId = merchantId; }
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
    public RiskLevel getRiskLevel() { return riskLevel; }
    public void setRiskLevel(RiskLevel riskLevel) { this.riskLevel = riskLevel; }
    public String getReasons() { return reasons; }
    public void setReasons(String reasons) { this.reasons = reasons; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
