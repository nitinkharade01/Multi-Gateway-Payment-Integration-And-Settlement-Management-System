package com.fintek.fraud.mapper;

import com.fintek.fraud.dto.response.FraudAlertResponse;
import com.fintek.fraud.entity.FraudAlert;
import org.springframework.stereotype.Component;

@Component
public class FraudAlertMapper {
    public FraudAlertResponse response(FraudAlert alert) {
        return new FraudAlertResponse(alert.getAlertId(), alert.getMerchantId(), alert.getTransactionId(), alert.getScore(),
                alert.getRiskLevel(), alert.getReasons(), alert.getCreatedAt());
    }
}
