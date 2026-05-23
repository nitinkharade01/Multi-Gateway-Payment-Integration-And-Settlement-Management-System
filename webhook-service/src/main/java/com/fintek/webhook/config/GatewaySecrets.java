package com.fintek.webhook.config;

import com.fintek.webhook.enums.GatewayName;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class GatewaySecrets {
    private final String razorpay;
    private final String cashfree;
    private final String payu;

    public GatewaySecrets(@Value("${webhook.secrets.razorpay}") String razorpay,
                          @Value("${webhook.secrets.cashfree}") String cashfree,
                          @Value("${webhook.secrets.payu}") String payu) {
        this.razorpay = razorpay;
        this.cashfree = cashfree;
        this.payu = payu;
    }

    public String forGateway(GatewayName gateway) {
        return switch (gateway) {
            case RAZORPAY -> razorpay;
            case CASHFREE -> cashfree;
            case PAYU -> payu;
        };
    }
}
