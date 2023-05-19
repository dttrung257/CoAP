package com.uet.CoAPapi.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uet.CoAPapi.coap.client.Client;
import com.uet.CoAPapi.coap.client.Sensor;
import com.uet.CoAPapi.coap.message.ControlMessage;
import com.uet.CoAPapi.coap.server.GatewayMonitor;
import com.uet.CoAPapi.repositories.SensorRepo;
import com.uet.CoAPapi.coap.server.Gateway;
import lombok.RequiredArgsConstructor;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.elements.exception.ConnectorException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class CoapConfig {
    private final Gateway gateway;
    private final SensorRepo sensorRepo;
    public static long maxNode = 0;
    public static List<Sensor> sensors = new ArrayList<>();
    private static final ObjectMapper mapper = new ObjectMapper();
    @Bean
    public void startGateway() {
        gateway.start();
    }

    private void loadMaxNode() {
        maxNode = GatewayMonitor.evaluateMaxNode();
        // maxNode = 1;
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
        loadMaxNode();
        if (maxNode > 0) {
            sensors = sensorRepo.findAllWithLimit(maxNode);
        } else {
            sensors = sensorRepo.findAll();
        }
        sensors.forEach(Sensor::loadInitData);
        sensors.forEach(System.out::println);
        sensors.forEach(s -> {
            (new Client(s)).createConnection();
        });
    }
}
