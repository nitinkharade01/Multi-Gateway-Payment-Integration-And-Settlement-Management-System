package com.fintek.reconciliation.controller;

import com.fintek.reconciliation.dto.request.RunReconciliationRequest;
import com.fintek.reconciliation.dto.response.*;
import com.fintek.reconciliation.service.ReconciliationService;
import jakarta.validation.Valid;
import java.io.IOException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/reconciliation")
public class ReconciliationController {
    private final ReconciliationService reconciliation;

    public ReconciliationController(ReconciliationService reconciliation) {
        this.reconciliation = reconciliation;
    }

    @PostMapping("/upload")
    CsvUploadResponse upload(@RequestParam("file") MultipartFile file) throws IOException {
        return reconciliation.upload(file.getBytes());
    }

    @PostMapping("/run")
    ReconciliationRunResponse run(@Valid @RequestBody RunReconciliationRequest request) {
        return reconciliation.run(request);
    }

    @GetMapping("/mismatches")
    Page<ReconciliationResultResponse> mismatches(Pageable pageable) {
        return reconciliation.mismatches(pageable);
    }
}
