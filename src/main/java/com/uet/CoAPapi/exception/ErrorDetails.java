package com.uet.CoAPapi.exception;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ErrorDetails {
    @JsonFormat(pattern = "dd-MM-yyyy hh:mm:ss", timezone = "GMT+7")
    private final Date timestamp;
    private final Integer status;
    private final String message;
    private final Object details;
}
