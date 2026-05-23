package com.fintek.settlement.service.impl;

import com.fintek.settlement.dto.request.GenerateSettlementRequest;
import com.fintek.settlement.dto.response.*;
import com.fintek.settlement.exception.SettlementException;
import com.fintek.settlement.mapper.SettlementMapper;
import com.fintek.settlement.repository.SettlementRepository;
import com.fintek.settlement.service.SettlementTransactionSource;
import com.fintek.settlement.validator.SettlementValidator;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SettlementServiceImplTest {
    private SettlementRepository settlements;
    private SettlementTransactionSource transactions;
    private SettlementServiceImpl service;
    private Instant from;
    private Instant to;

    @BeforeEach
    void setUp() {
        settlements = mock(SettlementRepository.class);
        transactions = mock(SettlementTransactionSource.class);
        service = new SettlementServiceImpl(settlements, transactions, new SettlementValidator(), new SettlementMapper(),
                mock(SettlementEventPublisher.class));
        from = Instant.parse("2026-05-01T00:00:00Z");
        to = Instant.parse("2026-05-02T00:00:00Z");
    }

    @Test
    void settlementCalculationRoundsGatewayPlatformGstAndNet() {
        when(transactions.successfulTransactions("mrc_1", from, to)).thenReturn(List.of(
                tx("txn_1", "600.00", "SUCCESS"), tx("txn_2", "400.00", "SUCCESS"), tx("txn_3", "70.00", "REFUNDED")));

        SettlementResponse response = service.generate(request());

        assertEquals(new BigDecimal("1000.00"), response.grossAmount());
        assertEquals(new BigDecimal("20.00"), response.gatewayCharge());
        assertEquals(new BigDecimal("10.00"), response.platformFee());
        assertEquals(new BigDecimal("1.80"), response.gst());
        assertEquals(new BigDecimal("968.20"), response.netAmount());
        assertEquals(2, response.transactionCount());
    }

    @Test
    void duplicateSettlementPreventionRejectsSameRange() {
        when(settlements.existsByMerchantIdAndRangeStartAndRangeEnd("mrc_1", from, to)).thenReturn(true);

        assertThrows(SettlementException.class, () -> service.generate(request()));
        verifyNoInteractions(transactions);
    }

    private GenerateSettlementRequest request() {
        return new GenerateSettlementRequest("mrc_1", from, to, new BigDecimal("2.00"), new BigDecimal("1.00"));
    }

    private SettlementTransactionSnapshot tx(String id, String amount, String status) {
        return new SettlementTransactionSnapshot(id, "ord_" + id, "mrc_1", new BigDecimal(amount), "INR",
                status, "RAZORPAY_SIMULATOR", from.plusSeconds(60));
    }
}
