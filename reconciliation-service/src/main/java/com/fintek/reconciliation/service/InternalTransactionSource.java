package com.fintek.reconciliation.service;

import com.fintek.reconciliation.dto.response.InternalTransactionRecord;
import java.time.Instant;
import java.util.List;

public interface InternalTransactionSource {
    List<InternalTransactionRecord> successfulTransactions(String merchantId, Instant from, Instant to);
}
