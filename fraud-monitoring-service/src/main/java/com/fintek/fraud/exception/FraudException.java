package com.fintek.fraud.exception;

public class FraudException extends RuntimeException {
    private final int status;

    public FraudException(int status, String message) {
        super(message);
        this.status = status;
    }

    public int status() { return status; }
}
