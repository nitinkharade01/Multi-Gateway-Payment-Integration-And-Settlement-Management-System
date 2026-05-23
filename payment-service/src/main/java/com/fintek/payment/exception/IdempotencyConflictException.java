package com.fintek.payment.exception;

public class IdempotencyConflictException extends PaymentException {
    public IdempotencyConflictException(String message) {
        super(409, message);
    }
}
