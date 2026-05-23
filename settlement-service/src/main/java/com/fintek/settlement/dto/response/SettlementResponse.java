package com.fintek.settlement.dto.response;

import com.fintek.settlement.enums.SettlementStatus;
import java.math.BigDecimal;
import java.time.Instant;

public record SettlementResponse(String settlementId, String merchantId, Instant from, Instant to, int transactionCount,
                                 BigDecimal grossAmount, BigDecimal gatewayCharge, BigDecimal platformFee, BigDecimal gst,
                                 BigDecimal netAmount, SettlementStatus status, String reportCsv) {
}
