package com.fintek.refund.service;

import com.fintek.refund.enums.*;
import com.fintek.refund.exception.RefundException;
import com.fintek.refund.mapper.RefundMapper;
import com.fintek.refund.repository.RefundRepository;
import com.fintek.refund.service.impl.RefundEventPublisher;
import com.fintek.refund.service.impl.RefundServiceImpl;
import com.fintek.refund.support.RefundTestDataBuilder;
import com.fintek.refund.util.GatewayRefundSimulator;
import com.fintek.refund.validator.RefundValidator;
import java.math.BigDecimal;
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
class RefundServiceAdditionalTest {
    @Mock
    private RefundRepository refunds;
    @Mock
    private PaymentRefundClient payments;
    @Mock
    private GatewayRefundSimulator gateway;
    @Mock
    private RefundEventPublisher eventPublisher;

    private RefundServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new RefundServiceImpl(refunds, payments, gateway, new RefundValidator(), new RefundMapper(), eventPublisher);
        lenient().when(refunds.findByTransactionIdAndStatusIn(eq("txn_1"), any())).thenReturn(List.of());
        lenient().when(gateway.refund(any())).thenReturn("gwr_1");
    }

    @Test
    void shouldRejectRefundForFailedAndPendingTransaction() {
        when(payments.transaction("txn_1")).thenReturn(RefundTestDataBuilder.transaction(PaymentStatus.FAILED, new BigDecimal("100.00")));
        assertThrows(RefundException.class,
                () -> service.create(RefundTestDataBuilder.refundRequest(new BigDecimal("10.00"))),
                "Failed transactions should not be refunded");

        when(payments.transaction("txn_1")).thenReturn(RefundTestDataBuilder.transaction(PaymentStatus.PENDING, new BigDecimal("100.00")));
        assertThrows(RefundException.class,
                () -> service.create(RefundTestDataBuilder.refundRequest(new BigDecimal("10.00"))),
                "Pending transactions should not be refunded");
    }

    @Test
    void shouldCalculateAvailableRefundableAmountForMultiplePartialRefunds() {
        when(payments.transaction("txn_1")).thenReturn(RefundTestDataBuilder.transaction(PaymentStatus.PARTIALLY_REFUNDED,
                new BigDecimal("100.00")));
        when(refunds.findByTransactionIdAndStatusIn(eq("txn_1"), any())).thenReturn(List.of(
                RefundTestDataBuilder.refund("rfnd_1", new BigDecimal("25.00"), RefundStatus.REFUND_SUCCESS),
                RefundTestDataBuilder.refund("rfnd_2", new BigDecimal("10.00"), RefundStatus.REFUND_SUCCESS)));

        var response = service.create(RefundTestDataBuilder.refundRequest(new BigDecimal("40.00")));

        assertEquals(new BigDecimal("25.00"), response.remainingRefundableAmount(),
                "Remaining refundable amount should subtract previous successful refunds and current refund");
        verify(payments).update("txn_1", PaymentStatus.PARTIALLY_REFUNDED);
    }

    @Test
    void shouldHandleGatewayRefundFailureWithoutUpdatingPayment() {
        when(payments.transaction("txn_1")).thenReturn(RefundTestDataBuilder.transaction(PaymentStatus.SUCCESS, new BigDecimal("100.00")));
        when(gateway.refund(any())).thenThrow(new RuntimeException("gateway down"));

        assertThrows(RuntimeException.class,
                () -> service.create(RefundTestDataBuilder.refundRequest(new BigDecimal("10.00"))),
                "Gateway refund failures should surface to caller");
        verify(payments, never()).update(anyString(), any());
    }

    @Test
    void shouldReturnRefundByRefundIdAndRefundsByMerchantId() {
        var refund = RefundTestDataBuilder.refund("rfnd_1", new BigDecimal("10.00"), RefundStatus.REFUND_SUCCESS);
        when(refunds.findByRefundId("rfnd_1")).thenReturn(Optional.of(refund));
        when(refunds.findByMerchantId(eq("mrc_1"), any())).thenReturn(new PageImpl<>(List.of(refund)));
        when(payments.transaction("txn_1")).thenReturn(RefundTestDataBuilder.transaction(PaymentStatus.SUCCESS, new BigDecimal("100.00")));
        when(refunds.findByTransactionIdAndStatusIn(eq("txn_1"), any())).thenReturn(List.of(refund));

        assertEquals("rfnd_1", service.get("rfnd_1").refundId(), "Refund lookup should return requested refund");
        assertEquals(1, service.merchantRefunds("mrc_1", PageRequest.of(0, 10)).getTotalElements(),
                "Merchant refund page should include refund records");
    }
}
