package com.fintek.refund.service;

import com.fintek.refund.dto.request.CreateRefundRequest;
import com.fintek.refund.dto.response.RefundResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RefundService {
    RefundResponse create(CreateRefundRequest request);
    RefundResponse get(String refundId);
    Page<RefundResponse> merchantRefunds(String merchantId, Pageable pageable);
}
