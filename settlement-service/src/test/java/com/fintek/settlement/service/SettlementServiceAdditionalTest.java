package com.fintek.settlement.service;

import com.fintek.settlement.dto.request.GenerateSettlementRequest;
import com.fintek.settlement.exception.SettlementException;
import com.fintek.settlement.mapper.SettlementMapper;
import com.fintek.settlement.repository.SettlementRepository;
import com.fintek.settlement.service.impl.SettlementEventPublisher;
import com.fintek.settlement.service.impl.SettlementServiceImpl;
import com.fintek.settlement.support.SettlementTestDataBuilder;
import com.fintek.settlement.validator.SettlementValidator;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SettlementServiceAdditionalTest {
    @Mock
    private SettlementRepository settlements;
    @Mock
    private SettlementTransactionSource transactions;
    @Mock
    private SettlementEventPublisher events;

    private SettlementServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new SettlementServiceImpl(settlements, transactions, new SettlementValidator(), new SettlementMapper(), events);
    }

    @Test
    void shouldIncludeOnlySuccessfulEligibleTransactionsAndExcludeOthers() {
        when(transactions.successfulTransactions("mrc_1", SettlementTestDataBuilder.FROM, SettlementTestDataBuilder.TO))
                .thenReturn(List.of(
                        SettlementTestDataBuilder.tx("txn_1", "100.00", "SUCCESS"),
                        SettlementTestDataBuilder.tx("txn_2", "100.00", "FAILED"),
                        new com.fintek.settlement.dto.response.SettlementTransactionSnapshot("txn_3", "ord_3", "mrc_1",
                                new BigDecimal("100.00"), "USD", "SUCCESS", "PAYU_SIMULATOR",
                                SettlementTestDataBuilder.FROM.plusSeconds(60))));

        var response = service.generate(SettlementTestDataBuilder.request());

        assertEquals(1, response.transactionCount(), "Only successful INR transactions should be settled");
        assertEquals(new BigDecimal("100.00"), response.grossAmount(), "Gross amount should include eligible transaction only");
    }

    @Test
    void shouldUseScaleTwoAndRoundingModeHalfUpForDecimalAmounts() {
        when(transactions.successfulTransactions("mrc_1", SettlementTestDataBuilder.FROM, SettlementTestDataBuilder.TO))
                .thenReturn(List.of(SettlementTestDataBuilder.tx("txn_1", "10.235", "SUCCESS")));

        var response = service.generate(new GenerateSettlementRequest("mrc_1", SettlementTestDataBuilder.FROM,
                SettlementTestDataBuilder.TO, new BigDecimal("2.50"), new BigDecimal("1.25")));

        assertEquals(new BigDecimal("10.24"), response.grossAmount(), "Gross amount should round HALF_UP to scale two");
        assertEquals(2, response.netAmount().scale(), "Net amount should remain at scale two");
    }

    @Test
    void shouldRejectInvalidDateRangeAndRangesLongerThanThirtyOneDays() {
        assertThrows(SettlementException.class,
                () -> service.generate(new GenerateSettlementRequest("mrc_1", SettlementTestDataBuilder.TO,
                        SettlementTestDataBuilder.FROM, BigDecimal.ONE, BigDecimal.ONE)),
                "From date greater than to date should be rejected");

        assertThrows(SettlementException.class,
                () -> service.generate(new GenerateSettlementRequest("mrc_1", Instant.parse("2026-01-01T00:00:00Z"),
                        Instant.parse("2026-03-01T00:00:00Z"), BigDecimal.ONE, BigDecimal.ONE)),
                "Settlement ranges longer than 31 days should be rejected");
    }

    @Test
    void shouldGenerateEmptySettlementWhenNoEligibleTransactions() {
        when(transactions.successfulTransactions("mrc_1", SettlementTestDataBuilder.FROM, SettlementTestDataBuilder.TO))
                .thenReturn(List.of());

        var response = service.generate(SettlementTestDataBuilder.request());

        assertEquals(0, response.transactionCount(), "No eligible transactions should create an empty generated batch");
        assertEquals(new BigDecimal("0.00"), response.netAmount(), "Empty settlements should have zero net amount");
    }

    @Test
    void shouldReturnSettlementByIdAndSettlementsByMerchant() {
        var settlement = SettlementTestDataBuilder.settlement();
        when(settlements.findBySettlementId("set_1")).thenReturn(Optional.of(settlement));
        when(settlements.findByMerchantId(eq("mrc_1"), any())).thenReturn(new PageImpl<>(List.of(settlement)));

        assertEquals("set_1", service.get("set_1").settlementId(), "Settlement lookup should return requested settlement");
        assertEquals(1, service.merchant("mrc_1", PageRequest.of(0, 10)).getTotalElements(),
                "Merchant settlement page should include settlement records");
    }
}
