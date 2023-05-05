package com.uet.CoAPapi.repositories;

import com.uet.CoAPapi.coap.client.Sensor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SensorRepo extends JpaRepository<Sensor, Long> {
    boolean existsByName(String name);
}
