package com.uet.CoAPapi.config;

import com.uet.CoAPapi.client.Client;
import com.uet.CoAPapi.client.Sensor;
import com.uet.CoAPapi.repositories.SensorRepo;
import com.uet.CoAPapi.server.Gateway;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Date;

@Configuration
@RequiredArgsConstructor
public class CoapConfig {
    private final Gateway gateway;
    private final SensorRepo sensorRepo;
    @Bean
    public void startGateway() {
        gateway.start();
    }

    @Bean
    public void testClient() {
        Sensor sensor = new Sensor(0.5);
        Sensor sensor1 = new Sensor(0.3);
        sensorRepo.save(sensor);
        sensorRepo.save(sensor1);
        Client client = new Client(sensor);
        Client client1 = new Client(sensor1);
        client.createConnection();
        client1.createConnection();
    }
}
