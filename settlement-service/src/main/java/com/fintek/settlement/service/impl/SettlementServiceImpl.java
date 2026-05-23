package com.fintek.settlement.service.impl;

import com.fintek.common.events.SettlementGeneratedEvent;
import com.fintek.common.money.Money;
import com.fintek.settlement.dto.request.GenerateSettlementRequest;
import com.fintek.settlement.dto.response.*;
import com.fintek.settlement.entity.Settlement;
import com.fintek.settlement.enums.SettlementStatus;
import com.fintek.settlement.exception.SettlementException;
import com.fintek.settlement.mapper.SettlementMapper;
import com.fintek.settlement.repository.SettlementRepository;
import com.fintek.settlement.service.*;
import com.fintek.settlement.util.SettlementReports;
import com.fintek.settlement.validator.SettlementValidator;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SettlementServiceImpl implements SettlementService {
    private static final Logger log = LoggerFactory.getLogger(SettlementServiceImpl.class);
    private final SettlementRepository settlements;
    private final SettlementTransactionSource transactions;
    private final SettlementValidator validator;
    private final SettlementMapper mapper;
    private final SettlementEventPublisher events;

    public SettlementServiceImpl(SettlementRepository settlements, SettlementTransactionSource transactions,
                                 SettlementValidator validator, SettlementMapper mapper, SettlementEventPublisher events) {
        this.settlements = settlements;
        this.transactions = transactions;
        this.validator = validator;
        this.mapper = mapper;
        this.events = events;
    }

    @Override
    @Transactional
    public SettlementResponse generate(GenerateSettlementRequest request) {
        validator.validate(request);
        if (settlements.existsByMerchantIdAndRangeStartAndRangeEnd(request.merchantId(), request.from(), request.to())) {
            throw new SettlementException(409, "Settlement batch already exists for merchant date range");
        }
        List<SettlementTransactionSnapshot> eligible = transactions.successfulTransactions(request.merchantId(), request.from(), request.to())
                .stream().filter(tx -> "SUCCESS".equals(tx.status()) && "INR".equals(tx.currency())).toList();
        BigDecimal gross = Money.scale(eligible.stream().map(SettlementTransactionSnapshot::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        BigDecimal gatewayCharge = Money.percentage(gross, request.gatewayChargePercentage());
        BigDecimal platformFee = Money.percentage(gross, request.platformFeePercentage());
        BigDecimal gst = Money.percentage(platformFee, new BigDecimal("18.00"));
        BigDecimal net = Money.scale(gross.subtract(gatewayCharge).subtract(platformFee).subtract(gst));
        Settlement settlement = new Settlement();
        settlement.setId(UUID.randomUUID().toString());
        settlement.setSettlementId("set_" + UUID.randomUUID().toString().replace("-", ""));
        settlement.setMerchantId(request.merchantId());
        settlement.setRangeStart(request.from());
        settlement.setRangeEnd(request.to());
        settlement.setTransactionCount(eligible.size());
        settlement.setGrossAmount(gross);
        settlement.setGatewayCharge(gatewayCharge);
        settlement.setPlatformFee(platformFee);
        settlement.setGst(gst);
        settlement.setNetAmount(net);
        settlement.setStatus(SettlementStatus.GENERATED);
        settlement.setReportCsv(SettlementReports.csv(eligible));
        settlement.setCreatedAt(Instant.now());
        settlements.save(settlement);
        events.publish(new SettlementGeneratedEvent(UUID.randomUUID().toString(), Instant.now(), request.merchantId(),
                settlement.getSettlementId(), net));
        log.info("Generated settlement {} for merchant {} gross {} net {}", settlement.getSettlementId(), request.merchantId(), gross, net);
        return mapper.response(settlement);
    }

    @Override
    @Transactional(readOnly = true)
    public SettlementResponse get(String settlementId) {
        return settlements.findBySettlementId(settlementId).map(mapper::response)
                .orElseThrow(() -> new SettlementException(404, "Settlement not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SettlementResponse> merchant(String merchantId, Pageable pageable) {
        return settlements.findByMerchantId(merchantId, pageable).map(mapper::response);
    }
}
