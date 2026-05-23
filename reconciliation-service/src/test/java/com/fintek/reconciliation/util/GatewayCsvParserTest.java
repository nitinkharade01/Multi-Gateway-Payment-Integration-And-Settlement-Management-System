package com.fintek.reconciliation.util;

import com.fintek.reconciliation.exception.ReconciliationException;
import java.io.IOException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GatewayCsvParserTest {
    private final GatewayCsvParser parser = new GatewayCsvParser();

    @Test
    void shouldParseCsvRecords() throws IOException {
        var records = parser.parse(resource("gateway-settlement-valid.csv"));

        assertEquals(2, records.size(), "Valid gateway CSV should parse all rows");
        assertEquals("txn_1", records.getFirst().transactionId(), "Transaction ID should be parsed");
    }

    @Test
    void shouldRejectInvalidCsvFormatAndMissingRequiredColumns() throws IOException {
        assertThrows(ReconciliationException.class, () -> parser.parse(resource("gateway-settlement-invalid.csv")),
                "CSV without required amount column should be rejected");
        assertThrows(ReconciliationException.class, () -> parser.parse("not,a,valid\n1,2,3\n".getBytes()),
                "Invalid CSV structure should be rejected");
    }

    private byte[] resource(String name) throws IOException {
        try (var input = getClass().getClassLoader().getResourceAsStream(name)) {
            assertNotNull(input, "Test CSV resource should exist: " + name);
            return input.readAllBytes();
        }
    }
}
