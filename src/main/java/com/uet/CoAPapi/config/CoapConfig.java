package com.uet.CoAPapi.config;

import com.uet.CoAPapi.coap.client.Client;
import com.uet.CoAPapi.coap.client.Sensor;
import com.uet.CoAPapi.repositories.SensorRepo;
import com.uet.CoAPapi.coap.server.Gateway;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class CoapConfig {
    private final Gateway gateway;
    private final SensorRepo sensorRepo;
    public static List<Sensor> sensors = new ArrayList<>();
    @Bean
    public void startGateway() {
        gateway.start();
    }

//    @Bean
//    public void testClient() {
//        Sensor sensor = new Sensor(0.5);
//        Sensor sensor1 = new Sensor(0.3);
//        sensorRepo.save(sensor);
//        sensorRepo.save(sensor1);
//    }

    @Bean
    public void loadSensors() {
        sensors = sensorRepo.findAll();
        sensors.forEach(Sensor::loadInitData);
        sensors.forEach(System.out::println);
        sensors.forEach(s -> {
            (new Client(s)).createConnection();
        });
    }
}
