package com.uet.CoAPapi.dtos;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SensorDto {
    private final long id;
    private final String name;
    private final long delay;
    private final String state;
}
