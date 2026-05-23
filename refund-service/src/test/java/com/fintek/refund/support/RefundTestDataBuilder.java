package com.fintek.refund.support;

import com.fintek.refund.dto.request.CreateRefundRequest;
import com.fintek.refund.dto.response.PaymentTransactionSnapshot;
import com.fintek.refund.entity.RefundRecord;
import com.fintek.refund.enums.PaymentStatus;
import com.fintek.refund.enums.RefundStatus;
import java.math.BigDecimal;
import java.time.Instant;

public final class RefundTestDataBuilder {
    private RefundTestDataBuilder() {
    }

    public static CreateRefundRequest refundRequest(BigDecimal amount) {
        return new CreateRefundRequest("mrc_1", "txn_1", amount, "customer cancellation");
    }

    public static PaymentTransactionSnapshot transaction(PaymentStatus status, BigDecimal amount) {
        return new PaymentTransactionSnapshot("txn_1", "ord_1", "mrc_1", amount, "INR", status,
                "RAZORPAY_SIMULATOR", Instant.parse("2026-05-01T10:15:30Z"));
    }

    public static RefundRecord refund(String id, BigDecimal amount, RefundStatus status) {
        RefundRecord refund = new RefundRecord();
        refund.setId("db_" + id);
        refund.setRefundId(id);
        refund.setMerchantId("mrc_1");
        refund.setTransactionId("txn_1");
        refund.setAmount(amount);
        refund.setStatus(status);
        refund.setReason("customer cancellation");
        refund.setGatewayReference("gwr_" + id);
        refund.setCreatedAt(Instant.parse("2026-05-01T10:15:30Z"));
        refund.setUpdatedAt(refund.getCreatedAt());
        return refund;
    }
}
