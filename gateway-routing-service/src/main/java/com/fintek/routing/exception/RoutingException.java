package com.fintek.routing.exception;

public class RoutingException extends RuntimeException {
    private final int status;

    public RoutingException(int status, String message) {
        super(message);
        this.status = status;
    }

    public int status() { return status; }
}
