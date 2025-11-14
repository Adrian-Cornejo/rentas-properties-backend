package com.rentas.properties.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class PropertyHasActiveContractsException extends RuntimeException {

    public PropertyHasActiveContractsException(String message) {
        super(message);
    }
}