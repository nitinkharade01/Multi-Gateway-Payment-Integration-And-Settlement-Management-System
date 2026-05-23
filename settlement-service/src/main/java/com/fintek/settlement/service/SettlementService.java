package com.fintek.settlement.service;

import com.fintek.settlement.dto.request.GenerateSettlementRequest;
import com.fintek.settlement.dto.response.SettlementResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SettlementService {
    SettlementResponse generate(GenerateSettlementRequest request);
    SettlementResponse get(String settlementId);
    Page<SettlementResponse> merchant(String merchantId, Pageable pageable);
}
