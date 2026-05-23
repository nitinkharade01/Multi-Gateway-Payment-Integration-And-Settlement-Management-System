package com.fintek.reconciliation.exception;

public class ReconciliationException extends RuntimeException {
    private final int status;

    public ReconciliationException(int status, String message) {
        super(message);
        this.status = status;
    }

    public int status() { return status; }
}
