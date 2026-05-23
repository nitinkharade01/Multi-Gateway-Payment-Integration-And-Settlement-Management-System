package com.fintek.payment.validator;

import com.fintek.common.money.Money;
import com.fintek.payment.dto.request.CreatePaymentOrderRequest;
import com.fintek.payment.dto.response.MerchantCredentialResponse;
import com.fintek.payment.exception.PaymentException;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class PaymentOrderValidator {
    public String currency(CreatePaymentOrderRequest request) {
        String currency = request.currency() == null || request.currency().isBlank() ? "INR" : request.currency().trim();
        if (!"INR".equals(currency)) {
            throw new PaymentException(400, "Only INR currency is currently supported");
        }
        return currency;
    }

    public void validateLimit(CreatePaymentOrderRequest request, MerchantCredentialResponse merchant) {
        BigDecimal amount = Money.scale(request.amount());
        if (amount.signum() <= 0) {
            throw new PaymentException(400, "Payment amount must be greater than zero");
        }
        if (!merchant.valid() || merchant.singlePaymentLimit() == null) {
            throw new PaymentException(403, "Merchant credentials are not approved for payments");
        }
        if (amount.compareTo(merchant.singlePaymentLimit()) > 0) {
            throw new PaymentException(422, "Payment amount exceeds merchant single payment limit");
        }
    }
}
