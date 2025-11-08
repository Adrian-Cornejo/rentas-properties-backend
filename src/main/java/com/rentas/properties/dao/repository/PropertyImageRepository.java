package com.rentas.properties.dao.repository;

import com.rentas.properties.dao.entity.PropertyImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PropertyImageRepository extends JpaRepository<PropertyImage, UUID> {

    List<PropertyImage> findByPropertyId(UUID propertyId);

    List<PropertyImage> findByPropertyIdOrderByDisplayOrderAsc(UUID propertyId);

    Optional<PropertyImage> findByPropertyIdAndIsMainTrue(UUID propertyId);

    void deleteByPropertyId(UUID propertyId);
}