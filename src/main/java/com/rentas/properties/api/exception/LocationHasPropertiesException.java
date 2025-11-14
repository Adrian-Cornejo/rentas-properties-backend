package com.rentas.properties.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class LocationHasPropertiesException extends RuntimeException {

    public LocationHasPropertiesException(String message) {
        super(message);
    }
}