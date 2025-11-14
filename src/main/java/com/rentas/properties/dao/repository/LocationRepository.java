package com.rentas.properties.dao.repository;

import com.rentas.properties.dao.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    List<Location> findByOrganizationId(UUID organizationId);

    List<Location> findByOrganizationIdAndIsActiveTrue(UUID organizationId);

    Optional<Location> findByNameAndOrganizationId(String name, UUID organizationId);

    boolean existsByNameAndOrganizationId(String name, UUID organizationId);

    @Query("SELECT COUNT(l) FROM Location l WHERE l.organization.id = :organizationId AND l.isActive = true")
    Long countActiveByOrganizationId(@Param("organizationId") UUID organizationId);
}