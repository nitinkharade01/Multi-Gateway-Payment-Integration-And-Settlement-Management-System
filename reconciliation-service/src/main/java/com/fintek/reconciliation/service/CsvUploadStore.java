package com.fintek.reconciliation.service;

import com.fintek.reconciliation.dto.response.GatewaySettlementRecord;
import com.fintek.reconciliation.exception.ReconciliationException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class CsvUploadStore {
    private final Map<String, List<GatewaySettlementRecord>> uploads = new ConcurrentHashMap<>();

    public String store(List<GatewaySettlementRecord> records) {
        String id = "upl_" + UUID.randomUUID().toString().replace("-", "");
        uploads.put(id, List.copyOf(records));
        return id;
    }

    public List<GatewaySettlementRecord> get(String id) {
        List<GatewaySettlementRecord> records = uploads.get(id);
        if (records == null) {
            throw new ReconciliationException(404, "Uploaded gateway CSV was not found");
        }
        return records;
    }
}
