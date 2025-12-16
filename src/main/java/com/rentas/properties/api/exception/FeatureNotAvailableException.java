package com.rentas.properties.api.exception;

public class FeatureNotAvailableException extends RuntimeException {

    public FeatureNotAvailableException(String message) {
        super(message);
    }

    public FeatureNotAvailableException(String message, Throwable cause) {
        super(message, cause);
    }
}