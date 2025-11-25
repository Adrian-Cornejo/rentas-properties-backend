package com.rentas.properties.api.exception;

public class InvalidContractDatesException extends RuntimeException {
    public InvalidContractDatesException(String message) {
        super(message);
    }
}