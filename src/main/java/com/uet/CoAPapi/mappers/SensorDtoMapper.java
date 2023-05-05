package com.uet.CoAPapi.mappers;

import com.uet.CoAPapi.coap.client.Sensor;
import com.uet.CoAPapi.dtos.SensorDto;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service
public class SensorDtoMapper implements Function<Sensor, SensorDto> {
    private static final String STATE_ON = "ON";
    private static final String STATE_OFF = "OFF";
    @Override
    public SensorDto apply(Sensor sensor) {
        return SensorDto.builder()
                .id(sensor.getId())
                .name(sensor.getName())
                .delay(sensor.getDelay())
                .state(sensor.isRunning() ? STATE_ON : STATE_OFF)
                .build();
    }
}
