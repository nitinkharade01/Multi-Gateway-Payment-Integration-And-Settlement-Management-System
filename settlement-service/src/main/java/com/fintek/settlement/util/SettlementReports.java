package com.fintek.settlement.util;

import com.fintek.settlement.dto.response.SettlementTransactionSnapshot;
import java.util.List;

public final class SettlementReports {
    private SettlementReports() {
    }

    public static String csv(List<SettlementTransactionSnapshot> eligible) {
        StringBuilder csv = new StringBuilder("transaction_id,order_id,gateway,amount,currency,created_at\n");
        eligible.forEach(tx -> csv.append(tx.transactionId()).append(',').append(tx.orderId()).append(',')
                .append(tx.gateway()).append(',').append(tx.amount()).append(',').append(tx.currency()).append(',')
                .append(tx.createdAt()).append('\n'));
        return csv.toString();
    }
}
