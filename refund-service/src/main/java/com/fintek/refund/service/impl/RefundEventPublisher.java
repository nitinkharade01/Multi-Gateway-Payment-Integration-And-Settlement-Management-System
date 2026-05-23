package com.fintek.refund.service.impl;

import com.fintek.common.events.RefundSuccessEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class RefundEventPublisher {
    private static final Logger log = LoggerFactory.getLogger(RefundEventPublisher.class);
    private final KafkaTemplate<String, Object> kafka;
    private final boolean kafkaEnabled;

    public RefundEventPublisher(KafkaTemplate<String, Object> kafka) {
        this(kafka, true);
    }

    @Autowired
    public RefundEventPublisher(KafkaTemplate<String, Object> kafka,
                                @Value("${app.kafka.enabled:true}") boolean kafkaEnabled) {
        this.kafka = kafka;
        this.kafkaEnabled = kafkaEnabled;
    }

    public void publish(RefundSuccessEvent event) {
        if (!kafkaEnabled) {
            log.info("Kafka disabled; refund-success event {} for merchant {} handled in demo mode",
                    event.eventId(), event.merchantId());
            return;
        }
        kafka.send("payment-events", event.merchantId(), event);
    }
}
