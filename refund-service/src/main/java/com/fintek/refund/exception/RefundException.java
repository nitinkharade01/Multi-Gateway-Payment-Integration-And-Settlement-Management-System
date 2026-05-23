package com.fintek.refund.exception;

public class RefundException extends RuntimeException {
    private final int status;

    public RefundException(int status, String message) {
        super(message);
        this.status = status;
    }

    public int status() { return status; }
}
