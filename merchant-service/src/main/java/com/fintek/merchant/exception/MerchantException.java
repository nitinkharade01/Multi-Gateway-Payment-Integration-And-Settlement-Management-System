package com.fintek.merchant.exception;

public class MerchantException extends RuntimeException {
    private final int status;

    public MerchantException(int status, String message) {
        super(message);
        this.status = status;
    }

    public int status() {
        return status;
    }
}
