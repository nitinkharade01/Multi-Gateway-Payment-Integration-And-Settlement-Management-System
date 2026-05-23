package com.fintek.merchant.entity;

import com.fintek.merchant.enums.KycStatus;
import com.fintek.merchant.enums.MerchantStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "merchants", indexes = @Index(name = "ix_merchant_email", columnList = "email", unique = true))
public class Merchant {
    @Id
    private String id;
    @Column(nullable = false, length = 200)
    private String businessName;
    @Column(nullable = false, unique = true, length = 190)
    private String email;
    @Column(nullable = false, length = 24)
    private String phone;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private MerchantStatus status;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private KycStatus kycStatus;
    @Column(length = 500)
    private String webhookUrl;
    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal singlePaymentLimit;
    @Column(nullable = false)
    private Instant createdAt;
    @Column(nullable = false)
    private Instant updatedAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getBusinessName() { return businessName; }
    public void setBusinessName(String businessName) { this.businessName = businessName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public MerchantStatus getStatus() { return status; }
    public void setStatus(MerchantStatus status) { this.status = status; }
    public KycStatus getKycStatus() { return kycStatus; }
    public void setKycStatus(KycStatus kycStatus) { this.kycStatus = kycStatus; }
    public String getWebhookUrl() { return webhookUrl; }
    public void setWebhookUrl(String webhookUrl) { this.webhookUrl = webhookUrl; }
    public BigDecimal getSinglePaymentLimit() { return singlePaymentLimit; }
    public void setSinglePaymentLimit(BigDecimal singlePaymentLimit) { this.singlePaymentLimit = singlePaymentLimit; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
