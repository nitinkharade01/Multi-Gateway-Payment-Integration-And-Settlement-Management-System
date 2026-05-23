package com.fintek.settlement.support;

import com.fintek.settlement.dto.request.GenerateSettlementRequest;
import com.fintek.settlement.dto.response.SettlementTransactionSnapshot;
import com.fintek.settlement.entity.Settlement;
import com.fintek.settlement.enums.SettlementStatus;
import java.math.BigDecimal;
import java.time.Instant;

public final class SettlementTestDataBuilder {
    public static final Instant FROM = Instant.parse("2026-05-01T00:00:00Z");
    public static final Instant TO = Instant.parse("2026-05-02T00:00:00Z");

    private SettlementTestDataBuilder() {
    }

    public static GenerateSettlementRequest request() {
        return new GenerateSettlementRequest("mrc_1", FROM, TO, new BigDecimal("2.00"), new BigDecimal("1.00"));
    }

    public static SettlementTransactionSnapshot tx(String id, String amount, String status) {
        return new SettlementTransactionSnapshot(id, "ord_" + id, "mrc_1", new BigDecimal(amount),
                "INR", status, "RAZORPAY_SIMULATOR", FROM.plusSeconds(60));
    }

    public static Settlement settlement() {
        Settlement settlement = new Settlement();
        settlement.setId("db_set_1");
        settlement.setSettlementId("set_1");
        settlement.setMerchantId("mrc_1");
        settlement.setRangeStart(FROM);
        settlement.setRangeEnd(TO);
        settlement.setTransactionCount(1);
        settlement.setGrossAmount(new BigDecimal("100.00"));
        settlement.setGatewayCharge(new BigDecimal("2.00"));
        settlement.setPlatformFee(new BigDecimal("1.00"));
        settlement.setGst(new BigDecimal("0.18"));
        settlement.setNetAmount(new BigDecimal("96.82"));
        settlement.setStatus(SettlementStatus.GENERATED);
        settlement.setReportCsv("transaction_id,amount,status\n");
        settlement.setCreatedAt(FROM);
        return settlement;
    }
}
