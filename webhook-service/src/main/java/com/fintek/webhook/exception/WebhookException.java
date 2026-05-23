package com.fintek.webhook.exception;

public class WebhookException extends RuntimeException {
    private final int status;

    public WebhookException(int status, String message) {
        super(message);
        this.status = status;
    }

    public int status() { return status; }
}
