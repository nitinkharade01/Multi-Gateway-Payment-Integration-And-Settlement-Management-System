package com.fintek.reconciliation.dto.response;

public record CsvUploadResponse(String uploadId, int records, String message) {
}
