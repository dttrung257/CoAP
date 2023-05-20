package com.uet.CoAPapi.dtos;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ControlSensorResponse {
    private String timestamp;
    private final List<ControlMessageDto> controlMessageDtos;
    private final String controlMessage;
    private final SensorDto sensor;
}
