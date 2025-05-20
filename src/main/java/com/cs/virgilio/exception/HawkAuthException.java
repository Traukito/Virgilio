package com.cs.virgilio.exception;

public class HawkAuthException extends RuntimeException {
    public HawkAuthException(String message) {
        super(message);
    }

    public HawkAuthException(String message, Throwable cause) {
        super(message, cause);
    }
}
