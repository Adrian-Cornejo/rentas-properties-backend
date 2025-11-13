package com.rentas.properties.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class LocationAlreadyExistsException extends RuntimeException {

    public LocationAlreadyExistsException(String message) {
        super(message);
    }

    public LocationAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}