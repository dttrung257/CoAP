package com.uet.CoAPapi.dtos;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ControlMessageDto {
    private final String message;
}
