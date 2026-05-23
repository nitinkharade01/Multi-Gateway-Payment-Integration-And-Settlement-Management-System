package com.fintek.refund.service.impl;

import com.fintek.refund.dto.request.CreateRefundRequest;
import com.fintek.refund.dto.response.*;
import com.fintek.refund.entity.RefundRecord;
import com.fintek.refund.enums.PaymentStatus;
import com.fintek.refund.exception.RefundException;
import com.fintek.refund.mapper.RefundMapper;
import com.fintek.refund.repository.RefundRepository;
import com.fintek.refund.service.PaymentRefundClient;
import com.fintek.refund.util.GatewayRefundSimulator;
import com.fintek.refund.validator.RefundValidator;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class RefundServiceImplTest {
    private RefundRepository refunds;
    private PaymentRefundClient payments;
    private GatewayRefundSimulator gateway;
    private RefundServiceImpl service;

    @BeforeEach
    void setUp() {
        refunds = mock(RefundRepository.class);
        payments = mock(PaymentRefundClient.class);
        gateway = mock(GatewayRefundSimulator.class);
        when(payments.transaction("txn_1")).thenReturn(paid("1000.00"));
        when(refunds.findByTransactionIdAndStatusIn(eq("txn_1"), any())).thenReturn(List.of());
        when(gateway.refund(any())).thenReturn("gwr_1");
        service = new RefundServiceImpl(refunds, payments, gateway, new RefundValidator(), new RefundMapper(),
                mock(RefundEventPublisher.class));
    }

    @Test
    void fullRefundSuccessMarksPaymentRefunded() {
        RefundResponse response = service.create(request("1000.00"));

        assertEquals(new BigDecimal("0.00"), response.remainingRefundableAmount());
        verify(payments).update("txn_1", PaymentStatus.REFUNDED);
    }

    @Test
    void partialRefundSuccessMarksPaymentPartiallyRefunded() {
        RefundResponse response = service.create(request("250.00"));

        assertEquals(new BigDecimal("750.00"), response.remainingRefundableAmount());
        verify(payments).update("txn_1", PaymentStatus.PARTIALLY_REFUNDED);
    }

    @Test
    void refundAmountGreaterThanRefundableAmountRejected() {
        assertThrows(RefundException.class, () -> service.create(request("1000.01")));
        verifyNoInteractions(gateway);
    }

    private CreateRefundRequest request(String amount) {
        return new CreateRefundRequest("mrc_1", "txn_1", new BigDecimal(amount), "customer cancellation");
    }

    private PaymentTransactionSnapshot paid(String amount) {
        return new PaymentTransactionSnapshot("txn_1", "ord_1", "mrc_1", new BigDecimal(amount), "INR",
                PaymentStatus.SUCCESS, "RAZORPAY_SIMULATOR", Instant.now());
    }
}
