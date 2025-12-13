package com.rentas.properties.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class NotificationProviderException extends RuntimeException {

    public NotificationProviderException(String message) {
        super(message);
    }

    public NotificationProviderException(String message, Throwable cause) {
        super(message, cause);
    }
}