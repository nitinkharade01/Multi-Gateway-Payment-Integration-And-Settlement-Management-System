package com.fintek.settlement.service;

import com.fintek.settlement.dto.response.SettlementTransactionSnapshot;
import java.time.Instant;
import java.util.List;

public interface SettlementTransactionSource {
    List<SettlementTransactionSnapshot> successfulTransactions(String merchantId, Instant from, Instant to);
}
