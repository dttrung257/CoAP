package com.uet.CoAPapi.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class CannotDeleteRunningSensorException extends RuntimeException {
    public CannotDeleteRunningSensorException(String message) {
        super(message);
    }
}