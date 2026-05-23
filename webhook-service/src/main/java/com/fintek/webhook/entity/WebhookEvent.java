package com.fintek.webhook.entity;

import com.fintek.webhook.enums.GatewayName;
import com.fintek.webhook.enums.WebhookProcessingStatus;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "webhook_events",
        uniqueConstraints = @UniqueConstraint(name = "uk_gateway_event", columnNames = {"gateway", "gatewayEventId"}))
public class WebhookEvent {
    @Id
    private String id;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private GatewayName gateway;
    @Column(nullable = false, length = 120)
    private String gatewayEventId;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String rawPayload;
    @Column(nullable = false, length = 96)
    private String receivedSignature;
    @Column(nullable = false)
    private Instant gatewayTimestamp;
    @Column(length = 72)
    private String transactionId;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private WebhookProcessingStatus status;
    @Column(nullable = false)
    private Instant processedAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public GatewayName getGateway() { return gateway; }
    public void setGateway(GatewayName gateway) { this.gateway = gateway; }
    public String getGatewayEventId() { return gatewayEventId; }
    public void setGatewayEventId(String gatewayEventId) { this.gatewayEventId = gatewayEventId; }
    public String getRawPayload() { return rawPayload; }
    public void setRawPayload(String rawPayload) { this.rawPayload = rawPayload; }
    public String getReceivedSignature() { return receivedSignature; }
    public void setReceivedSignature(String receivedSignature) { this.receivedSignature = receivedSignature; }
    public Instant getGatewayTimestamp() { return gatewayTimestamp; }
    public void setGatewayTimestamp(Instant gatewayTimestamp) { this.gatewayTimestamp = gatewayTimestamp; }
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    public WebhookProcessingStatus getStatus() { return status; }
    public void setStatus(WebhookProcessingStatus status) { this.status = status; }
    public Instant getProcessedAt() { return processedAt; }
    public void setProcessedAt(Instant processedAt) { this.processedAt = processedAt; }
}
