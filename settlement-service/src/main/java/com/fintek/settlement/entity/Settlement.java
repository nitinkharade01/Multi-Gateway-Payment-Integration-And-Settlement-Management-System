package com.fintek.settlement.entity;

import com.fintek.settlement.enums.SettlementStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "settlements",
        uniqueConstraints = @UniqueConstraint(name = "uk_settlement_merchant_range", columnNames = {"merchantId", "rangeStart", "rangeEnd"}))
public class Settlement {
    @Id
    private String id;
    @Column(nullable = false, unique = true, length = 72)
    private String settlementId;
    @Column(nullable = false, length = 72)
    private String merchantId;
    @Column(nullable = false)
    private Instant rangeStart;
    @Column(nullable = false)
    private Instant rangeEnd;
    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal grossAmount;
    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal gatewayCharge;
    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal platformFee;
    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal gst;
    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal netAmount;
    @Column(nullable = false)
    private int transactionCount;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private SettlementStatus status;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String reportCsv;
    @Column(nullable = false)
    private Instant createdAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getSettlementId() { return settlementId; }
    public void setSettlementId(String settlementId) { this.settlementId = settlementId; }
    public String getMerchantId() { return merchantId; }
    public void setMerchantId(String merchantId) { this.merchantId = merchantId; }
    public Instant getRangeStart() { return rangeStart; }
    public void setRangeStart(Instant rangeStart) { this.rangeStart = rangeStart; }
    public Instant getRangeEnd() { return rangeEnd; }
    public void setRangeEnd(Instant rangeEnd) { this.rangeEnd = rangeEnd; }
    public BigDecimal getGrossAmount() { return grossAmount; }
    public void setGrossAmount(BigDecimal grossAmount) { this.grossAmount = grossAmount; }
    public BigDecimal getGatewayCharge() { return gatewayCharge; }
    public void setGatewayCharge(BigDecimal gatewayCharge) { this.gatewayCharge = gatewayCharge; }
    public BigDecimal getPlatformFee() { return platformFee; }
    public void setPlatformFee(BigDecimal platformFee) { this.platformFee = platformFee; }
    public BigDecimal getGst() { return gst; }
    public void setGst(BigDecimal gst) { this.gst = gst; }
    public BigDecimal getNetAmount() { return netAmount; }
    public void setNetAmount(BigDecimal netAmount) { this.netAmount = netAmount; }
    public int getTransactionCount() { return transactionCount; }
    public void setTransactionCount(int transactionCount) { this.transactionCount = transactionCount; }
    public SettlementStatus getStatus() { return status; }
    public void setStatus(SettlementStatus status) { this.status = status; }
    public String getReportCsv() { return reportCsv; }
    public void setReportCsv(String reportCsv) { this.reportCsv = reportCsv; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
