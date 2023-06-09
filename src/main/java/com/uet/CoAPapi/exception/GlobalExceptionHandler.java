package com.uet.CoAPapi.exception;

import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.util.Date;
import java.util.concurrent.RejectedExecutionException;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDetails> handleGlobalException(Exception e, WebRequest webRequest) {
        final ErrorDetails ed = ErrorDetails.builder()
                .timestamp(new Date())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message(e.getMessage())
                .details(webRequest.getDescription(false))
                .build();
        return new ResponseEntity<>(ed, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDetails> handleValidationException(MethodArgumentNotValidException e) {
        final ErrorDetails ed = ErrorDetails.builder()
                .timestamp(new Date())
                .status(HttpStatus.BAD_REQUEST.value())
                .message("Validation Error")
                .details(e.getAllErrors()
                        .stream()
                        .map(DefaultMessageSourceResolvable::getDefaultMessage)
                        .toList())
                .build();
        return new ResponseEntity<>(ed, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UnknownSensorStateException.class)
    public ResponseEntity<ErrorDetails> handleUnknownSensorStateException(UnknownSensorStateException e, WebRequest webRequest) {
        final ErrorDetails ed = ErrorDetails.builder()
                .timestamp(new Date())
                .status(HttpStatus.BAD_REQUEST.value())
                .message(e.getMessage())
                .details(webRequest.getDescription(false))
                .build();
        return new ResponseEntity<>(ed, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(SensorAlreadyExistsException.class)
    public ResponseEntity<ErrorDetails> handleSensorAlreadyExistsException(SensorAlreadyExistsException e, WebRequest webRequest) {
        final ErrorDetails ed = ErrorDetails.builder()
                .timestamp(new Date())
                .status(HttpStatus.BAD_REQUEST.value())
                .message(e.getMessage())
                .details(webRequest.getDescription(false))
                .build();
        return new ResponseEntity<>(ed, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(SensorNotFoundException.class)
    public ResponseEntity<ErrorDetails> handleSensorNotFoundException(SensorNotFoundException e, WebRequest webRequest) {
        final ErrorDetails ed = ErrorDetails.builder()
                .timestamp(new Date())
                .status(HttpStatus.NOT_FOUND.value())
                .message(e.getMessage())
                .details(webRequest.getDescription(false))
                .build();
        return new ResponseEntity<>(ed, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(CannotDeleteRunningSensorException.class)
    public ResponseEntity<ErrorDetails> handleCannotDeleteRunningSensorException(CannotDeleteRunningSensorException e, WebRequest webRequest) {
        final ErrorDetails ed = ErrorDetails.builder()
                .timestamp(new Date())
                .status(HttpStatus.BAD_REQUEST.value())
                .message(e.getMessage())
                .details(webRequest.getDescription(false))
                .build();
        return new ResponseEntity<>(ed, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ReachMaxNodeException.class)
    public ResponseEntity<ErrorDetails> handleReachMaxNodeException(ReachMaxNodeException e, WebRequest webRequest) {
        final ErrorDetails ed = ErrorDetails.builder()
                .timestamp(new Date())
                .status(HttpStatus.BAD_REQUEST.value())
                .message(e.getMessage())
                .details(webRequest.getDescription(false))
                .build();
        return new ResponseEntity<>(ed, HttpStatus.BAD_REQUEST);
    }
}

