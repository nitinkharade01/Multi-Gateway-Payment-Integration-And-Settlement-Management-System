package com.fintek.settlement.mapper;

import com.fintek.settlement.dto.response.SettlementResponse;
import com.fintek.settlement.entity.Settlement;
import org.springframework.stereotype.Component;

@Component
public class SettlementMapper {
    public SettlementResponse response(Settlement settlement) {
        return new SettlementResponse(settlement.getSettlementId(), settlement.getMerchantId(), settlement.getRangeStart(),
                settlement.getRangeEnd(), settlement.getTransactionCount(), settlement.getGrossAmount(),
                settlement.getGatewayCharge(), settlement.getPlatformFee(), settlement.getGst(), settlement.getNetAmount(),
                settlement.getStatus(), settlement.getReportCsv());
    }
}
