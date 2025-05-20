package com.cs.virgilio.exception;

public class HawkServiceException extends RuntimeException {
    public HawkServiceException(String message) {
        super(message);
    }

    public HawkServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
