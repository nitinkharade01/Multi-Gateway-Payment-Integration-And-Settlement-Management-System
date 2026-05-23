package com.fintek.fraud.service.impl;

import com.fintek.common.events.FraudAlertCreatedEvent;
import com.fintek.common.money.Money;
import com.fintek.fraud.dto.request.FraudAssessmentRequest;
import com.fintek.fraud.dto.response.*;
import com.fintek.fraud.entity.*;
import com.fintek.fraud.enums.*;
import com.fintek.fraud.mapper.FraudAlertMapper;
import com.fintek.fraud.repository.*;
import com.fintek.fraud.service.*;
import com.fintek.fraud.validator.FraudRequestValidator;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FraudScoringServiceImpl implements FraudScoringService {
    private final FraudEvidenceProvider evidence;
    private final FraudAlertRepository alerts;
    private final FraudSignalRepository signals;
    private final FraudEventPublisher events;
    private final FraudAlertMapper mapper;
    private final FraudRequestValidator validator;

    public FraudScoringServiceImpl(FraudEvidenceProvider evidence, FraudAlertRepository alerts, FraudSignalRepository signals,
                                   FraudEventPublisher events, FraudAlertMapper mapper, FraudRequestValidator validator) {
        this.evidence = evidence;
        this.alerts = alerts;
        this.signals = signals;
        this.events = events;
        this.mapper = mapper;
        this.validator = validator;
    }

    @Override
    @Transactional
    public FraudAssessmentResponse assess(FraudAssessmentRequest request) {
        validator.validate(request);
        Instant now = Instant.now();
        Set<FraudRule> rules = EnumSet.noneOf(FraudRule.class);
        int score = 0;
        if (request.amount().compareTo(new BigDecimal("100000")) > 0) {
            rules.add(FraudRule.HIGH_VALUE_TRANSACTION);
            score += 45;
        }
        if (evidence.merchantPaymentsAfter(request.merchantId(), now.minus(10, ChronoUnit.MINUTES)) >= 5) {
            rules.add(FraudRule.SUSPICIOUS_FREQUENCY);
            score += 30;
        }
        if (evidence.merchantFailuresAfter(request.merchantId(), now.minus(15, ChronoUnit.MINUTES)) >= 3) {
            rules.add(FraudRule.MULTIPLE_FAILED_ATTEMPTS);
            score += 25;
        }
        if (request.recentRefundRequests() > 2) {
            rules.add(FraudRule.REFUND_ABUSE);
            score += 20;
        }
        if (evidence.customerPaymentsAfter(request.customerPhone(), now.minus(10, ChronoUnit.MINUTES)) >= 8) {
            rules.add(FraudRule.CUSTOMER_PATTERN_RISK);
            score += 20;
        }
        if (request.failedWebhookCallbacks() > 3) {
            rules.add(FraudRule.WEBHOOK_FAILURE_RISK);
            score += 15;
        }
        score = Math.min(score, 100);
        RiskLevel level = riskLevel(score);
        signals.save(signal(request, now));
        FraudAlert alert = level == RiskLevel.LOW ? null : alerts.save(alert(request, rules, score, level, now));
        if (alert != null) {
            events.publish(new FraudAlertCreatedEvent(UUID.randomUUID().toString(), now, request.merchantId(),
                    request.transactionId(), score, level.name(), alert.getReasons()));
        }
        return new FraudAssessmentResponse(request.merchantId(), request.transactionId(), score, level,
                Set.copyOf(rules), alert == null ? null : alert.getAlertId());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FraudAlertResponse> alerts(String merchantId, Pageable pageable) {
        return alerts.findByMerchantId(merchantId, pageable).map(mapper::response);
    }

    public RiskLevel riskLevel(int score) {
        int band = score <= 40 ? 0 : score <= 70 ? 1 : 2;
        return switch (band) {
            case 0 -> RiskLevel.LOW;
            case 1 -> RiskLevel.MEDIUM;
            default -> RiskLevel.HIGH;
        };
    }

    private FraudSignal signal(FraudAssessmentRequest request, Instant now) {
        FraudSignal signal = new FraudSignal();
        signal.setId(UUID.randomUUID().toString());
        signal.setMerchantId(request.merchantId());
        signal.setTransactionId(request.transactionId());
        signal.setCustomerPhone(request.customerPhone());
        signal.setAmount(Money.scale(request.amount()));
        signal.setStatus(request.status().toUpperCase(Locale.ROOT));
        signal.setObservedAt(now);
        return signal;
    }

    private FraudAlert alert(FraudAssessmentRequest request, Set<FraudRule> rules, int score, RiskLevel level, Instant now) {
        FraudAlert alert = new FraudAlert();
        alert.setId(UUID.randomUUID().toString());
        alert.setAlertId("fra_" + UUID.randomUUID().toString().replace("-", ""));
        alert.setMerchantId(request.merchantId());
        alert.setTransactionId(request.transactionId());
        alert.setScore(score);
        alert.setRiskLevel(level);
        alert.setReasons(rules.stream().map(Enum::name).sorted().collect(Collectors.joining(",")));
        alert.setCreatedAt(now);
        return alert;
    }
}
