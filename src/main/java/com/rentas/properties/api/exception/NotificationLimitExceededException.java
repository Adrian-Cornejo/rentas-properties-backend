package com.rentas.properties.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
public class NotificationLimitExceededException extends RuntimeException {

    public NotificationLimitExceededException(String message) {
        super(message);
    }
}