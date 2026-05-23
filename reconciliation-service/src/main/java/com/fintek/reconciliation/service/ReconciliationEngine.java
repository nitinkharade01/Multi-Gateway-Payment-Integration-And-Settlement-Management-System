package com.fintek.reconciliation.service;

import com.fintek.common.money.Money;
import com.fintek.reconciliation.dto.response.*;
import com.fintek.reconciliation.entity.ReconciliationResult;
import com.fintek.reconciliation.enums.ReconciliationStatus;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class ReconciliationEngine {
    public List<ReconciliationResult> compare(String runId, String merchantId, List<InternalTransactionRecord> internal,
                                              List<GatewaySettlementRecord> gateway) {
        Map<String, InternalTransactionRecord> internalById = internal.stream()
                .collect(Collectors.toMap(InternalTransactionRecord::transactionId, Function.identity(), (left, right) -> left));
        Map<String, List<GatewaySettlementRecord>> gatewayById = gateway.stream()
                .collect(Collectors.groupingBy(GatewaySettlementRecord::transactionId, LinkedHashMap::new, Collectors.toList()));
        List<ReconciliationResult> results = new ArrayList<>();
        gatewayById.forEach((transactionId, rows) -> {
            if (rows.size() > 1) {
                results.add(result(runId, merchantId, internalById.get(transactionId), rows.getFirst(),
                        ReconciliationStatus.DUPLICATE_GATEWAY_RECORD,
                        "Gateway CSV contains " + rows.size() + " rows for transaction"));
                return;
            }
            GatewaySettlementRecord row = rows.getFirst();
            InternalTransactionRecord tx = internalById.get(transactionId);
            if (tx == null) {
                results.add(result(runId, merchantId, null, row, ReconciliationStatus.MISSING_INTERNAL,
                        "Gateway record has no internal transaction"));
            } else if (Money.scale(tx.amount()).compareTo(Money.scale(row.amount())) != 0) {
                results.add(result(runId, merchantId, tx, row, ReconciliationStatus.AMOUNT_MISMATCH,
                        "Internal and gateway amounts differ"));
            } else if (!normalize(tx.status()).equals(normalize(row.status()))) {
                results.add(result(runId, merchantId, tx, row, ReconciliationStatus.STATUS_MISMATCH,
                        "Internal status " + tx.status() + " differs from gateway status " + row.status()));
            } else {
                results.add(result(runId, merchantId, tx, row, ReconciliationStatus.MATCHED, "Exact transaction match"));
            }
        });
        internal.stream().filter(tx -> !gatewayById.containsKey(tx.transactionId()))
                .map(tx -> result(runId, merchantId, tx, null, ReconciliationStatus.MISSING_GATEWAY,
                        "Internal transaction is absent from gateway CSV")).forEach(results::add);
        return results;
    }

    private String normalize(String status) {
        return switch (status.toUpperCase(Locale.ROOT)) {
            case "PAID", "CAPTURED", "SUCCESS" -> "SUCCESS";
            case "FAILURE", "FAILED" -> "FAILED";
            default -> status.toUpperCase(Locale.ROOT);
        };
    }

    private ReconciliationResult result(String runId, String merchantId, InternalTransactionRecord internal,
                                        GatewaySettlementRecord gateway, ReconciliationStatus status, String reason) {
        ReconciliationResult result = new ReconciliationResult();
        result.setId(UUID.randomUUID().toString());
        result.setRunId(runId);
        result.setMerchantId(merchantId);
        result.setTransactionId(internal != null ? internal.transactionId() : gateway.transactionId());
        result.setInternalAmount(internal == null ? null : Money.scale(internal.amount()));
        result.setGatewayAmount(gateway == null ? null : Money.scale(gateway.amount()));
        result.setInternalStatus(internal == null ? null : internal.status());
        result.setGatewayStatus(gateway == null ? null : gateway.status());
        result.setStatus(status);
        result.setReason(reason);
        result.setCreatedAt(Instant.now());
        return result;
    }
}
