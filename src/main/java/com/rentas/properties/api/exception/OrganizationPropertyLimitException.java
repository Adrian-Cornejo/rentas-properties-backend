package com.rentas.properties.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class OrganizationPropertyLimitException extends RuntimeException {

    public OrganizationPropertyLimitException(String message) {
        super(message);
    }
}