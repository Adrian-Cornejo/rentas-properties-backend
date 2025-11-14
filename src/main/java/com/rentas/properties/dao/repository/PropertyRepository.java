package com.rentas.properties.dao.repository;

import com.rentas.properties.dao.entity.Property;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PropertyRepository extends JpaRepository<Property, UUID> {

    Optional<Property> findByPropertyCode(String propertyCode);

    List<Property> findByStatus(String status);

    List<Property> findByStatusAndIsActiveTrue(String status);

    List<Property> findByLocation_Id(UUID locationId);

    List<Property> findByPropertyType(String propertyType);

    boolean existsByPropertyCode(String propertyCode);

    List<Property> findByOrganization_Id(UUID organizationId);

    List<Property> findByOrganization_IdAndStatus(UUID organizationId, String status);

    List<Property> findByOrganization_IdAndIsActiveTrue(UUID organizationId);

    @Query("SELECT COUNT(p) FROM Property p WHERE p.organization.id = :organizationId AND p.isActive = true")
    Long countActiveByOrganization_Id(@Param("organizationId") UUID organizationId);

    @Query("SELECT p FROM Property p WHERE p.organization.id = :organizationId AND p.status = 'DISPONIBLE' AND p.isActive = true")
    List<Property> findAvailableByOrganization(@Param("organizationId") UUID organizationId);

    @Query("SELECT p FROM Property p WHERE p.organization.id = :organizationId AND p.status = 'RENTADA'")
    List<Property> findRentedByOrganization(@Param("organizationId") UUID organizationId);
}