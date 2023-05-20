package com.uet.CoAPapi.mappers;

import com.uet.CoAPapi.coap.message.ControlMessage;
import com.uet.CoAPapi.dtos.ControlMessageDto;
import com.uet.CoAPapi.utils.TimeUtil;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service
public class ControlMessageDtoMapper implements Function<ControlMessage, ControlMessageDto> {
    @Override
    public ControlMessageDto apply(ControlMessage controlMessage) {
        String controlForSensor = (controlMessage.getSensorId().equalsIgnoreCase("ALL")) ?
                "All sensor" : "Sensor id: " + controlMessage.getSensorId();
        final String message;
        if (controlMessage.getDelay() == -1) {
            message = switch (controlMessage.getMessage()) {
                case ControlMessage.TURN_ON_MESSAGE -> " turn on";
                case ControlMessage.TURN_OFF_MESSAGE -> " turn off";
                default -> " terminate";
            };
        } else {
            message = " change speed to " + controlMessage.getDelay() / 1000.0 + " seconds";
        }
        return ControlMessageDto.builder()
                .timestamp(TimeUtil.format(controlMessage.getTimestamp()))
                .message(controlForSensor + message)
                .build();
    }
}
