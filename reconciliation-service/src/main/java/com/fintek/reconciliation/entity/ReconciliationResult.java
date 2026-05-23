package com.fintek.reconciliation.entity;

import com.fintek.reconciliation.enums.ReconciliationStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "reconciliation_results", indexes = @Index(name = "ix_recon_transaction", columnList = "transactionId"))
public class ReconciliationResult {
    @Id
    private String id;
    @Column(nullable = false, length = 72)
    private String runId;
    @Column(nullable = false, length = 72)
    private String merchantId;
    @Column(nullable = false, length = 72)
    private String transactionId;
    @Column(precision = 18, scale = 2)
    private BigDecimal internalAmount;
    @Column(precision = 18, scale = 2)
    private BigDecimal gatewayAmount;
    @Column(length = 40)
    private String internalStatus;
    @Column(length = 40)
    private String gatewayStatus;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 48)
    private ReconciliationStatus status;
    @Column(nullable = false, length = 600)
    private String reason;
    @Column(nullable = false)
    private Instant createdAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getRunId() { return runId; }
    public void setRunId(String runId) { this.runId = runId; }
    public String getMerchantId() { return merchantId; }
    public void setMerchantId(String merchantId) { this.merchantId = merchantId; }
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    public BigDecimal getInternalAmount() { return internalAmount; }
    public void setInternalAmount(BigDecimal internalAmount) { this.internalAmount = internalAmount; }
    public BigDecimal getGatewayAmount() { return gatewayAmount; }
    public void setGatewayAmount(BigDecimal gatewayAmount) { this.gatewayAmount = gatewayAmount; }
    public String getInternalStatus() { return internalStatus; }
    public void setInternalStatus(String internalStatus) { this.internalStatus = internalStatus; }
    public String getGatewayStatus() { return gatewayStatus; }
    public void setGatewayStatus(String gatewayStatus) { this.gatewayStatus = gatewayStatus; }
    public ReconciliationStatus getStatus() { return status; }
    public void setStatus(ReconciliationStatus status) { this.status = status; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
