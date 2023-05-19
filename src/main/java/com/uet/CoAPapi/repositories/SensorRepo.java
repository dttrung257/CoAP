package com.uet.CoAPapi.repositories;

import com.uet.CoAPapi.coap.client.Sensor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SensorRepo extends JpaRepository<Sensor, Long> {
    boolean existsById(long id);
    boolean existsByName(String name);

    @Query("SELECT s FROM Sensor s ORDER BY s.id ASC LIMIT :limit")
    List<Sensor> findAllWithLimit(@Param("limit") long limit);

    @Query("SELECT s FROM Sensor s WHERE s.id != :id AND s.name = :name")
    List<Sensor> findByNameWithOtherId(@Param("id") long id, @Param("name") String name);
}
