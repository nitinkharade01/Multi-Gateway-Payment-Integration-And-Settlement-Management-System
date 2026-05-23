package com.fintek.reconciliation.support;

import com.fintek.reconciliation.dto.request.RunReconciliationRequest;
import com.fintek.reconciliation.dto.response.*;
import com.fintek.reconciliation.entity.ReconciliationResult;
import com.fintek.reconciliation.enums.ReconciliationStatus;
import java.math.BigDecimal;
import java.time.Instant;

public final class TestDataFactory {
    public static final Instant FROM = Instant.parse("2026-05-01T00:00:00Z");
    public static final Instant TO = Instant.parse("2026-05-02T00:00:00Z");

    private TestDataFactory() {
    }

    public static RunReconciliationRequest runRequest(String uploadId) {
        return new RunReconciliationRequest(uploadId, "mrc_1", FROM, TO);
    }

    public static InternalTransactionRecord internal(String id, String amount, String status) {
        return new InternalTransactionRecord(id, "ord_" + id, "mrc_1", new BigDecimal(amount), "INR",
                status, "RAZORPAY_SIMULATOR", FROM.plusSeconds(60));
    }

    public static GatewaySettlementRecord gateway(String id, String amount, String status) {
        return new GatewaySettlementRecord(id, new BigDecimal(amount), status, "gw_" + id);
    }

    public static ReconciliationResult result(String transactionId, ReconciliationStatus status) {
        ReconciliationResult result = new ReconciliationResult();
        result.setId("rec_" + transactionId);
        result.setRunId("run_1");
        result.setMerchantId("mrc_1");
        result.setTransactionId(transactionId);
        result.setInternalAmount(new BigDecimal("100.00"));
        result.setGatewayAmount(new BigDecimal("99.00"));
        result.setInternalStatus("SUCCESS");
        result.setGatewayStatus("PAID");
        result.setStatus(status);
        result.setReason("test");
        result.setCreatedAt(FROM);
        return result;
    }
}
