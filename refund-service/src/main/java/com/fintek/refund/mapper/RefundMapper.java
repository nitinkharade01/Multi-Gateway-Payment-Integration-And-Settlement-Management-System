package com.fintek.refund.mapper;

import com.fintek.refund.dto.response.RefundResponse;
import com.fintek.refund.entity.RefundRecord;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class RefundMapper {
    public RefundResponse response(RefundRecord refund, BigDecimal remaining) {
        return new RefundResponse(refund.getRefundId(), refund.getMerchantId(), refund.getTransactionId(), refund.getAmount(),
                remaining, refund.getStatus(), refund.getGatewayReference(), refund.getCreatedAt());
    }
}
