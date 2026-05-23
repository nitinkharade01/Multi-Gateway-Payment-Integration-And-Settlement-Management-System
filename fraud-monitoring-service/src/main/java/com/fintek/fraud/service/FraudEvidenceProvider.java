package com.fintek.fraud.service;

import java.time.Instant;

public interface FraudEvidenceProvider {
    long merchantPaymentsAfter(String merchantId, Instant after);
    long merchantFailuresAfter(String merchantId, Instant after);
    long customerPaymentsAfter(String phone, Instant after);
}
