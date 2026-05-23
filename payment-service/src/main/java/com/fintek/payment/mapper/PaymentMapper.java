package com.fintek.payment.mapper;

import com.fintek.payment.dto.response.*;
import com.fintek.payment.entity.PaymentOrder;
import com.fintek.payment.entity.TransactionEntity;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {
    public PaymentOrderResponse order(PaymentOrder order, TransactionEntity transaction, boolean replay) {
        return new PaymentOrderResponse(order.getOrderId(), transaction.getTransactionId(), order.getMerchantId(),
                order.getAmount(), order.getCurrency(), order.getPaymentMode(), order.getStatus(), transaction.getGateway(),
                transaction.getCheckoutUrl(), order.getExpiresAt(), replay);
    }

    public TransactionStatusResponse status(TransactionEntity tx) {
        return new TransactionStatusResponse(tx.getTransactionId(), tx.getOrder().getOrderId(), tx.getMerchantId(),
                tx.getAmount(), tx.getCurrency(), tx.getStatus(), tx.getGateway(), tx.getFailureReason(), tx.getUpdatedAt());
    }

    public TransactionSnapshotResponse snapshot(TransactionEntity tx) {
        return new TransactionSnapshotResponse(tx.getTransactionId(), tx.getOrder().getOrderId(), tx.getMerchantId(),
                tx.getAmount(), tx.getCurrency(), tx.getStatus(), tx.getGateway(), tx.getCreatedAt());
    }
}
