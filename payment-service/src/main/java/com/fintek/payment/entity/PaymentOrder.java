package com.fintek.payment.entity;

import com.fintek.payment.enums.PaymentMode;
import com.fintek.payment.enums.PaymentStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "payment_orders",
        uniqueConstraints = @UniqueConstraint(name = "uk_merchant_idempotency", columnNames = {"merchantId", "idempotencyKey"}),
        indexes = @Index(name = "ix_payment_order_order_id", columnList = "orderId", unique = true))
public class PaymentOrder {
    @Id
    private String id;
    @Column(nullable = false, unique = true, length = 72)
    private String orderId;
    @Column(nullable = false, length = 72)
    private String merchantId;
    @Column(nullable = false, length = 160)
    private String idempotencyKey;
    @Column(nullable = false, length = 128)
    private String requestFingerprint;
    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;
    @Column(nullable = false, length = 3)
    private String currency;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private PaymentMode paymentMode;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private PaymentStatus status;
    @Column(nullable = false, length = 190)
    private String customerEmail;
    @Column(nullable = false, length = 24)
    private String customerPhone;
    @Column(length = 500)
    private String returnUrl;
    @Column(length = 500)
    private String merchantWebhookUrl;
    @Column(nullable = false)
    private Instant createdAt;
    @Column(nullable = false)
    private Instant expiresAt;
    @Column(nullable = false)
    private Instant updatedAt;

    public boolean expiredAt(Instant now) {
        return expiresAt.isBefore(now) && status != PaymentStatus.SUCCESS && status != PaymentStatus.REFUNDED
                && status != PaymentStatus.PARTIALLY_REFUNDED;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public String getMerchantId() { return merchantId; }
    public void setMerchantId(String merchantId) { this.merchantId = merchantId; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
    public String getRequestFingerprint() { return requestFingerprint; }
    public void setRequestFingerprint(String requestFingerprint) { this.requestFingerprint = requestFingerprint; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public PaymentMode getPaymentMode() { return paymentMode; }
    public void setPaymentMode(PaymentMode paymentMode) { this.paymentMode = paymentMode; }
    public PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentStatus status) { this.status = status; }
    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }
    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }
    public String getReturnUrl() { return returnUrl; }
    public void setReturnUrl(String returnUrl) { this.returnUrl = returnUrl; }
    public String getMerchantWebhookUrl() { return merchantWebhookUrl; }
    public void setMerchantWebhookUrl(String merchantWebhookUrl) { this.merchantWebhookUrl = merchantWebhookUrl; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
