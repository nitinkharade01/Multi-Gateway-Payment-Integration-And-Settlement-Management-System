package com.fintek.fraud.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "fraud_signals", indexes = {
        @Index(name = "ix_signal_merchant_time", columnList = "merchantId,observedAt"),
        @Index(name = "ix_signal_phone_time", columnList = "customerPhone,observedAt")
})
public class FraudSignal {
    @Id
    private String id;
    @Column(nullable = false, length = 72)
    private String merchantId;
    @Column(nullable = false, length = 72)
    private String transactionId;
    @Column(nullable = false, length = 24)
    private String customerPhone;
    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;
    @Column(nullable = false, length = 32)
    private String status;
    @Column(nullable = false)
    private Instant observedAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getMerchantId() { return merchantId; }
    public void setMerchantId(String merchantId) { this.merchantId = merchantId; }
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getObservedAt() { return observedAt; }
    public void setObservedAt(Instant observedAt) { this.observedAt = observedAt; }
}
