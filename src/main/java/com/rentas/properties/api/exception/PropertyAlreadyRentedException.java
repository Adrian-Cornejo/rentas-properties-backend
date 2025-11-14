package com.rentas.properties.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class PropertyAlreadyRentedException extends RuntimeException {

    public PropertyAlreadyRentedException(String message) {
        super(message);
    }
}
