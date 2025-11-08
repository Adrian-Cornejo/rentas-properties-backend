package com.rentas.properties.dao.repository;

import com.rentas.properties.dao.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LocationRepository extends JpaRepository<Location, UUID> {

    Optional<Location> findByName(String name);

    List<Location> findByIsActiveTrue();

    List<Location> findByCity(String city);

    boolean existsByName(String name);
}