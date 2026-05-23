package com.fintek.merchant.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "merchant_api_keys", indexes = @Index(name = "ix_merchant_api_key", columnList = "apiKey", unique = true))
public class MerchantApiKey {
    @Id
    private String id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Merchant merchant;
    @Column(nullable = false, unique = true, length = 96)
    private String apiKey;
    @Column(nullable = false)
    private String secretHash;
    @Column(nullable = false)
    private boolean enabled;
    @Column(nullable = false)
    private Instant expiresAt;
    @Column(nullable = false)
    private Instant createdAt;
    private Instant disabledAt;

    public boolean usableAt(Instant now) {
        return enabled && expiresAt.isAfter(now);
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public Merchant getMerchant() { return merchant; }
    public void setMerchant(Merchant merchant) { this.merchant = merchant; }
    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }
    public String getSecretHash() { return secretHash; }
    public void setSecretHash(String secretHash) { this.secretHash = secretHash; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getDisabledAt() { return disabledAt; }
    public void setDisabledAt(Instant disabledAt) { this.disabledAt = disabledAt; }
}
