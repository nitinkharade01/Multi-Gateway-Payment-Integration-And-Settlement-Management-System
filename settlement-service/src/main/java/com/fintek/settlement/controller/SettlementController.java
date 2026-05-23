package com.fintek.settlement.controller;

import com.fintek.settlement.dto.request.GenerateSettlementRequest;
import com.fintek.settlement.dto.response.SettlementResponse;
import com.fintek.settlement.service.SettlementService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/settlements")
public class SettlementController {
    private final SettlementService settlements;

    public SettlementController(SettlementService settlements) {
        this.settlements = settlements;
    }

    @PostMapping("/generate")
    @ResponseStatus(HttpStatus.CREATED)
    SettlementResponse generate(@Valid @RequestBody GenerateSettlementRequest request) {
        return settlements.generate(request);
    }

    @GetMapping("/merchant/{merchantId}")
    Page<SettlementResponse> merchant(@PathVariable String merchantId, Pageable pageable) {
        return settlements.merchant(merchantId, pageable);
    }

    @GetMapping("/{settlementId}")
    SettlementResponse settlement(@PathVariable String settlementId) {
        return settlements.get(settlementId);
    }
}
