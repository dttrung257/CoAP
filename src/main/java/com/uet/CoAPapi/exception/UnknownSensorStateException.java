package com.uet.CoAPapi.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class UnknownSensorStateException extends RuntimeException {
    public UnknownSensorStateException(String message) {
        super(message);
    }
}
