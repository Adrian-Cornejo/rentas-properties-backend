package com.rentas.properties.business.services.impl;

import com.rentas.properties.api.dto.response.PublicPropertyResponse;
import com.rentas.properties.api.exception.PropertyNotFoundException;
import com.rentas.properties.business.services.PublicPropertyService;
import com.rentas.properties.dao.entity.Location;
import com.rentas.properties.dao.entity.Organization;
import com.rentas.properties.dao.entity.Property;
import com.rentas.properties.dao.entity.PropertyImage;
import com.rentas.properties.dao.repository.PropertyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PublicPropertyServiceImpl implements PublicPropertyService {

    private final PropertyRepository propertyRepository;

    @Override
    @Transactional(readOnly = true)
    public PublicPropertyResponse getPublicPropertyById(UUID propertyId) {
        log.info("Obteniendo información pública de propiedad con ID: {}", propertyId);

        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new PropertyNotFoundException(
                        "Propiedad no encontrada con ID: " + propertyId));

        // Validar que la propiedad esté activa
        if (!property.getIsActive()) {
            log.warn("Intento de acceder a propiedad inactiva: {}", propertyId);
            throw new PropertyNotFoundException(
                    "Propiedad no encontrada con ID: " + propertyId);
        }

        log.info("Propiedad pública obtenida exitosamente: {}", property.getPropertyCode());
        return mapToPublicResponse(property);
    }

    private PublicPropertyResponse mapToPublicResponse(Property property) {
        Location location = property.getLocation();
        Organization organization = property.getOrganization();

        List<String> imageUrls = property.getImages() != null ?
                property.getImages().stream()
                        .sorted((a, b) -> Integer.compare(a.getDisplayOrder(), b.getDisplayOrder()))
                        .map(PropertyImage::getImageUrl)
                        .collect(Collectors.toList())
                : new ArrayList<>();

        return PublicPropertyResponse.builder()
                .id(property.getId())
                .propertyCode(property.getPropertyCode())
                .propertyType(property.getPropertyType())
                .address(property.getAddress())
                .monthlyRent(property.getMonthlyRent())
                .floors(property.getFloors())
                .waterFee(property.getWaterFee())
                .bedrooms(property.getBedrooms())
                .bathrooms(property.getBathrooms())
                .halfBathrooms(property.getHalfBathrooms())
                .hasLivingRoom(property.getHasLivingRoom())
                .hasDiningRoom(property.getHasDiningRoom())
                .hasKitchen(property.getHasKitchen())
                .hasServiceArea(property.getHasServiceArea())
                .parkingSpaces(property.getParkingSpaces())
                .totalAreaM2(property.getTotalAreaM2())
                .includesWater(property.getIncludesWater())
                .includesElectricity(property.getIncludesElectricity())
                .includesGas(property.getIncludesGas())
                .includesInternet(property.getIncludesInternet())
                .notes(property.getNotes())
                .imageUrls(imageUrls)
                .locationName(location != null ? location.getName() : null)
                .city(location != null ? location.getCity() : null)
                .state(location != null ? location.getState() : null)
                .organizationName(organization != null ? organization.getName() : null)
                .organizationLogo(organization != null ? organization.getLogoUrl() : null)
                .organizationPrimaryColor(organization != null ? organization.getPrimaryColor() : null)
                .build();
    }
}