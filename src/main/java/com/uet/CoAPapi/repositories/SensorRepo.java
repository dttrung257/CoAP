package com.uet.CoAPapi.repositories;

import com.uet.CoAPapi.client.Sensor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SensorRepo extends JpaRepository<Sensor, Long> {
}
