package com.fintek.fraud.service.impl;

import com.fintek.fraud.repository.FraudSignalRepository;
import com.fintek.fraud.service.FraudEvidenceProvider;
import java.time.Instant;
import org.springframework.stereotype.Component;

@Component
public class JpaFraudEvidenceProvider implements FraudEvidenceProvider {
    private final FraudSignalRepository signals;

    public JpaFraudEvidenceProvider(FraudSignalRepository signals) {
        this.signals = signals;
    }

    @Override
    public long merchantPaymentsAfter(String merchantId, Instant after) {
        return signals.countByMerchantIdAndObservedAtAfter(merchantId, after);
    }

    @Override
    public long merchantFailuresAfter(String merchantId, Instant after) {
        return signals.countByMerchantIdAndStatusAndObservedAtAfter(merchantId, "FAILED", after);
    }

    @Override
    public long customerPaymentsAfter(String phone, Instant after) {
        return signals.countByCustomerPhoneAndObservedAtAfter(phone, after);
    }
}
