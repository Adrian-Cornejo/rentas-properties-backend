package com.rentas.properties.dao.repository;

import com.rentas.properties.dao.entity.Property;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PropertyRepository extends JpaRepository<Property, UUID> {

    Optional<Property> findByPropertyCode(String propertyCode);

    List<Property> findByStatus(String status);

    List<Property> findByStatusAndIsActiveTrue(String status);

    List<Property> findByLocationId(UUID locationId);

    List<Property> findByPropertyType(String propertyType);

    boolean existsByPropertyCode(String propertyCode);
}