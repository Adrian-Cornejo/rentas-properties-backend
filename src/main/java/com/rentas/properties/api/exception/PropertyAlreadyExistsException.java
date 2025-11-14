package com.rentas.properties.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class PropertyAlreadyExistsException extends RuntimeException {

    public PropertyAlreadyExistsException(String message) {
        super(message);
    }
}