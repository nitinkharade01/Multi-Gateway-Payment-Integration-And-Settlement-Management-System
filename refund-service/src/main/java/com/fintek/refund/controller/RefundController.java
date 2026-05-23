package com.fintek.refund.controller;

import com.fintek.refund.dto.request.CreateRefundRequest;
import com.fintek.refund.dto.response.RefundResponse;
import com.fintek.refund.service.RefundService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/refunds")
public class RefundController {
    private final RefundService refunds;

    public RefundController(RefundService refunds) {
        this.refunds = refunds;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    RefundResponse create(@Valid @RequestBody CreateRefundRequest request) {
        return refunds.create(request);
    }

    @GetMapping("/{refundId}")
    RefundResponse get(@PathVariable String refundId) {
        return refunds.get(refundId);
    }

    @GetMapping("/merchant/{merchantId}")
    Page<RefundResponse> merchant(@PathVariable String merchantId, Pageable pageable) {
        return refunds.merchantRefunds(merchantId, pageable);
    }
}
