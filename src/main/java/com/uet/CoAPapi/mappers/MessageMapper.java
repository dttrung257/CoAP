package com.uet.CoAPapi.mappers;

import com.uet.CoAPapi.coap.client.Sensor;
import com.uet.CoAPapi.coap.message.DataMessage;
import com.uet.CoAPapi.utils.TimeUtil;

import java.util.function.Function;

public class MessageMapper implements Function<Sensor, DataMessage> {
    private static final TimeUtil timeUtil = new TimeUtil();
    @Override
    public DataMessage apply(Sensor sensor) {
        return DataMessage.builder()
                .id(sensor.getId())
                .name(sensor.getName())
                .humidity(sensor.getHumidity())
                .timestamp(sensor.getTimestamp())
                .build();
    }
}
