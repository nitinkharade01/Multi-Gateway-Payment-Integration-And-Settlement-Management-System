package com.fintek.payment.service.impl;

import com.fintek.common.events.PaymentCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventPublisher {
    private static final Logger log = LoggerFactory.getLogger(PaymentEventPublisher.class);
    private final KafkaTemplate<String, Object> kafka;
    private final boolean kafkaEnabled;

    public PaymentEventPublisher(KafkaTemplate<String, Object> kafka) {
        this(kafka, true);
    }

    @Autowired
    public PaymentEventPublisher(KafkaTemplate<String, Object> kafka,
                                 @Value("${app.kafka.enabled:true}") boolean kafkaEnabled) {
        this.kafka = kafka;
        this.kafkaEnabled = kafkaEnabled;
    }

    public void paymentCreated(PaymentCreatedEvent event) {
        if (!kafkaEnabled) {
            log.info("Kafka disabled; payment-created event {} for transaction {} handled in demo mode",
                    event.eventId(), event.transactionId());
            return;
        }
        kafka.send("payment-events", event.merchantId(), event);
        log.info("Published payment-created event {} for transaction {}", event.eventId(), event.transactionId());
    }
}
