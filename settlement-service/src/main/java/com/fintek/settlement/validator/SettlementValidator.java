package com.fintek.settlement.validator;

import com.fintek.settlement.dto.request.GenerateSettlementRequest;
import com.fintek.settlement.exception.SettlementException;
import java.time.Duration;
import org.springframework.stereotype.Component;

@Component
public class SettlementValidator {
    public void validate(GenerateSettlementRequest request) {
        if (!request.to().isAfter(request.from())) {
            throw new SettlementException(400, "Settlement end time must be after start time");
        }
        if (Duration.between(request.from(), request.to()).toDays() > 31) {
            throw new SettlementException(400, "Settlement range cannot exceed 31 days");
        }
    }
}
