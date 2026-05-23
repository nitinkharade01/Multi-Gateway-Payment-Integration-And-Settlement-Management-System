package com.fintek.reconciliation.util;

import com.fintek.reconciliation.dto.response.GatewaySettlementRecord;
import com.fintek.reconciliation.exception.ReconciliationException;
import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.apache.commons.csv.CSVFormat;
import org.springframework.stereotype.Component;

@Component
public class GatewayCsvParser {
    public List<GatewaySettlementRecord> parse(byte[] csv) {
        try (Reader reader = new InputStreamReader(new ByteArrayInputStream(csv), StandardCharsets.UTF_8)) {
            CSVFormat format = CSVFormat.Builder.create(CSVFormat.DEFAULT).setHeader().setSkipHeaderRecord(true).build();
            return format.parse(reader).stream()
                    .map(record -> new GatewaySettlementRecord(require(record.get("transaction_id"), "transaction_id"),
                            new BigDecimal(require(record.get("amount"), "amount")),
                            require(record.get("status"), "status"), record.isMapped("gateway_reference")
                            ? record.get("gateway_reference") : "")).toList();
        } catch (IOException | RuntimeException error) {
            throw new ReconciliationException(400, "Gateway settlement CSV must contain transaction_id, amount and status");
        }
    }

    private String require(String value, String column) {
        if (value == null || value.isBlank()) {
            throw new ReconciliationException(400, "CSV column " + column + " cannot be blank");
        }
        return value.trim();
    }
}
