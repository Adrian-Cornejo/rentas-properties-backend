package com.rentas.properties.business.services.impl;

import com.rentas.properties.api.dto.request.CreatePropertyRequest;
import com.rentas.properties.api.dto.request.UpdatePropertyRequest;
import com.rentas.properties.api.dto.response.PropertyDetailResponse;
import com.rentas.properties.api.dto.response.PropertyResponse;
import com.rentas.properties.api.exception.*;
import com.rentas.properties.business.services.CloudinaryService;
import com.rentas.properties.business.services.PropertyService;
import com.rentas.properties.dao.entity.*;
import com.rentas.properties.dao.repository.ContractRepository;
import com.rentas.properties.dao.repository.LocationRepository;
import com.rentas.properties.dao.repository.OrganizationRepository;
import com.rentas.properties.dao.repository.PropertyRepository;
import com.rentas.properties.dao.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PropertyServiceImpl implements PropertyService {

    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;
    private final LocationRepository locationRepository;
    private final OrganizationRepository organizationRepository;
    private final ContractRepository contractRepository;
    private final CloudinaryService cloudinaryService;

    @Override
    @Transactional
    public PropertyDetailResponse createProperty(CreatePropertyRequest request) {
        log.info("Creando propiedad con código: {}", request.getPropertyCode());

        User currentUser = getCurrentUser();
        validateUserHasOrganization(currentUser);

        Organization organization = currentUser.getOrganization();

        if (!organization.canAddProperty()) {
            log.warn("Organización {} alcanzó límite de propiedades. Plan: {}, Actual: {}, Máximo: {}",
                    organization.getId(),
                    organization.getPlanCode(),
                    organization.getCurrentPropertiesCount(),
                    organization.getSubscriptionPlan().getMaxProperties());
            throw new OrganizationPropertyLimitException(
                    "Has alcanzado el límite máximo de propiedades (" + organization.getSubscriptionPlan().getMaxProperties() + ") " +
                            "según tu plan " + organization.getPlanCode() + ". " +
                            "Por favor, mejora tu plan para agregar más propiedades."
            );
        }

        if (propertyRepository.existsByPropertyCode(request.getPropertyCode())) {
            log.warn("Ya existe una propiedad con el código '{}'", request.getPropertyCode());
            throw new PropertyAlreadyExistsException(
                    "Ya existe una propiedad con el código '" + request.getPropertyCode() + "'"
            );
        }

        Location location = null;
        if (request.getLocationId() != null) {
            location = locationRepository.findById(request.getLocationId())
                    .orElseThrow(() -> new LocationNotFoundException(
                            "Ubicación no encontrada con ID: " + request.getLocationId()
                    ));

            if (!location.getOrganization().getId().equals(organization.getId())) {
                log.warn("La ubicación {} no pertenece a la organización {}",
                        request.getLocationId(), organization.getId());
                throw new UnauthorizedAccessException("La ubicación no pertenece a tu organización");
            }
        }

        Property property = Property.builder()
                .organization(organization)
                .location(location)
                .propertyCode(request.getPropertyCode())
                .propertyType(request.getPropertyType().toUpperCase())
                .address(request.getAddress())
                .monthlyRent(request.getMonthlyRent())
                .waterFee(request.getWaterFee() != null ? request.getWaterFee() : java.math.BigDecimal.valueOf(105.00))
                .status("DISPONIBLE")
                .floors(request.getFloors())
                .bedrooms(request.getBedrooms())
                .bathrooms(request.getBathrooms())
                .halfBathrooms(request.getHalfBathrooms())
                .hasLivingRoom(request.getHasLivingRoom())
                .hasDiningRoom(request.getHasDiningRoom())
                .hasKitchen(request.getHasKitchen())
                .hasServiceArea(request.getHasServiceArea())
                .parkingSpaces(request.getParkingSpaces())
                .totalAreaM2(request.getTotalAreaM2())
                .includesWater(request.getIncludesWater())
                .includesElectricity(request.getIncludesElectricity())
                .includesGas(request.getIncludesGas())
                .includesInternet(request.getIncludesInternet())
                .notes(request.getNotes())
                .isActive(true)
                .build();

        Property savedProperty = propertyRepository.save(property);

        if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
            processPropertyImages(savedProperty, request.getImageUrls(), organization);
        }

        organization.incrementPropertiesCount();
        organizationRepository.save(organization);

        log.info("Propiedad creada exitosamente con ID: {} - Contador de organización: {}/{}",
                savedProperty.getId(), organization.getCurrentPropertiesCount(), organization.getSubscriptionPlan().getMaxProperties());

        return mapToDetailResponse(savedProperty);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PropertyResponse> getAllProperties(boolean includeInactive) {
        log.info("Obteniendo todas las propiedades - includeInactive: {}", includeInactive);

        User currentUser = getCurrentUser();
        validateUserHasOrganization(currentUser);

        UUID organizationId = currentUser.getOrganization().getId();

        List<Property> properties;
        if (includeInactive) {
            properties = propertyRepository.findByOrganization_Id(organizationId);
        } else {
            properties = propertyRepository.findByOrganization_IdAndIsActiveTrue(organizationId);
        }

        log.debug("Se encontraron {} propiedades", properties.size());

        return properties.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PropertyDetailResponse getPropertyById(UUID id) {
        log.info("Obteniendo propiedad con ID: {}", id);

        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new PropertyNotFoundException("Propiedad no encontrada con ID: " + id));

        User currentUser = getCurrentUser();
        validateUserCanAccessProperty(currentUser, property);

        return mapToDetailResponse(property);
    }

    @Override
    @Transactional(readOnly = true)
    public PropertyDetailResponse getPropertyByCode(String code) {
        log.info("Buscando propiedad por código: {}", code);

        Property property = propertyRepository.findByPropertyCode(code)
                .orElseThrow(() -> new PropertyNotFoundException(
                        "Propiedad no encontrada con código: " + code
                ));

        User currentUser = getCurrentUser();
        validateUserCanAccessProperty(currentUser, property);

        return mapToDetailResponse(property);
    }

    @Override
    @Transactional
    public PropertyDetailResponse updateProperty(UUID id, UpdatePropertyRequest request) {
        log.info("Actualizando propiedad con ID: {}", id);

        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new PropertyNotFoundException("Propiedad no encontrada con ID: " + id));

        User currentUser = getCurrentUser();
        validateUserCanAccessProperty(currentUser, property);

        if (request.getLocationId() != null &&
                (property.getLocation() == null || !property.getLocation().getId().equals(request.getLocationId()))) {
            Location location = locationRepository.findById(request.getLocationId())
                    .orElseThrow(() -> new LocationNotFoundException(
                            "Ubicación no encontrada con ID: " + request.getLocationId()
                    ));

            if (!location.getOrganization().getId().equals(property.getOrganization().getId())) {
                throw new UnauthorizedAccessException("La ubicación no pertenece a tu organización");
            }

            property.setLocation(location);
        }

        if (request.getPropertyType() != null) {
            validatePropertyType(request.getPropertyType());
            property.setPropertyType(request.getPropertyType().toUpperCase());
        }

        if (request.getAddress() != null) {
            property.setAddress(request.getAddress());
        }

        if (request.getMonthlyRent() != null) {
            property.setMonthlyRent(request.getMonthlyRent());
        }

        if (request.getWaterFee() != null) {
            property.setWaterFee(request.getWaterFee());
        }

        if (request.getStatus() != null) {
            validatePropertyStatus(request.getStatus());
            property.setStatus(request.getStatus().toUpperCase());
        }

        if (request.getFloors() != null) {
            property.setFloors(request.getFloors());
        }

        if (request.getBedrooms() != null) {
            property.setBedrooms(request.getBedrooms());
        }

        if (request.getBathrooms() != null) {
            property.setBathrooms(request.getBathrooms());
        }

        if (request.getHalfBathrooms() != null) {
            property.setHalfBathrooms(request.getHalfBathrooms());
        }

        if (request.getHasLivingRoom() != null) {
            property.setHasLivingRoom(request.getHasLivingRoom());
        }

        if (request.getHasDiningRoom() != null) {
            property.setHasDiningRoom(request.getHasDiningRoom());
        }

        if (request.getHasKitchen() != null) {
            property.setHasKitchen(request.getHasKitchen());
        }

        if (request.getHasServiceArea() != null) {
            property.setHasServiceArea(request.getHasServiceArea());
        }

        if (request.getParkingSpaces() != null) {
            property.setParkingSpaces(request.getParkingSpaces());
        }

        if (request.getTotalAreaM2() != null) {
            property.setTotalAreaM2(request.getTotalAreaM2());
        }

        if (request.getIncludesWater() != null) {
            property.setIncludesWater(request.getIncludesWater());
        }

        if (request.getIncludesElectricity() != null) {
            property.setIncludesElectricity(request.getIncludesElectricity());
        }

        if (request.getIncludesGas() != null) {
            property.setIncludesGas(request.getIncludesGas());
        }

        if (request.getIncludesInternet() != null) {
            property.setIncludesInternet(request.getIncludesInternet());
        }

        if (request.getNotes() != null) {
            property.setNotes(request.getNotes());
        }

        if (request.getImageUrls() != null) {
            updatePropertyImages(property, request.getImageUrls(), property.getOrganization());
        }

        Property updatedProperty = propertyRepository.save(property);
        log.info("Propiedad actualizada exitosamente: {}", updatedProperty.getPropertyCode());

        return mapToDetailResponse(updatedProperty);
    }

    @Override
    @Transactional
    public void deleteProperty(UUID id) {
        log.info("Eliminando propiedad con ID: {}", id);

        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new PropertyNotFoundException("Propiedad no encontrada con ID: " + id));

        User currentUser = getCurrentUser();
        validateUserCanAccessProperty(currentUser, property);

        long activeContracts = contractRepository.findByPropertyId(id).stream()
                .filter(c -> "ACTIVO".equals(c.getStatus()))
                .count();

        if (activeContracts > 0) {
            log.warn("No se puede eliminar la propiedad {} porque tiene {} contratos activos",
                    id, activeContracts);
            throw new PropertyHasActiveContractsException(
                    "No se puede eliminar la propiedad porque tiene contratos activos. " +
                            "Primero debes finalizar o cancelar los contratos."
            );
        }

        List<PropertyImage> images = new ArrayList<>(property.getImages());
        for (PropertyImage image : images) {
            deleteImageFromCloudinary(image.getImageUrl());
        }

        Organization organization = property.getOrganization();

        organization.decrementPropertiesCount();
        organizationRepository.save(organization);

        propertyRepository.delete(property);
        log.info("Propiedad eliminada exitosamente: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PropertyResponse> getPropertiesByStatus(String status) {
        log.info("Obteniendo propiedades por estado: {}", status);

        validatePropertyStatus(status);

        User currentUser = getCurrentUser();
        validateUserHasOrganization(currentUser);

        UUID organizationId = currentUser.getOrganization().getId();

        List<Property> properties = propertyRepository.findByOrganization_IdAndStatus(
                organizationId, status.toUpperCase()
        );

        log.debug("Se encontraron {} propiedades con estado {}", properties.size(), status);

        return properties.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PropertyResponse> getPropertiesByLocation(UUID locationId) {
        log.info("Obteniendo propiedades de la ubicación: {}", locationId);

        User currentUser = getCurrentUser();
        validateUserHasOrganization(currentUser);

        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new LocationNotFoundException("Ubicación no encontrada con ID: " + locationId));

        if (!location.getOrganization().getId().equals(currentUser.getOrganization().getId())) {
            throw new UnauthorizedAccessException("La ubicación no pertenece a tu organización");
        }

        List<Property> properties = propertyRepository.findByLocation_Id(locationId);

        log.debug("Se encontraron {} propiedades", properties.size());

        return properties.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PropertyResponse> getPropertiesByType(String type) {
        log.info("Obteniendo propiedades por tipo: {}", type);

        validatePropertyType(type);

        User currentUser = getCurrentUser();
        validateUserHasOrganization(currentUser);

        UUID organizationId = currentUser.getOrganization().getId();

        List<Property> allProperties = propertyRepository.findByOrganization_Id(organizationId);
        List<Property> properties = allProperties.stream()
                .filter(p -> type.equalsIgnoreCase(p.getPropertyType()))
                .collect(Collectors.toList());

        log.debug("Se encontraron {} propiedades de tipo {}", properties.size(), type);

        return properties.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PropertyResponse> getAvailableProperties() {
        log.info("Obteniendo propiedades disponibles");

        User currentUser = getCurrentUser();
        validateUserHasOrganization(currentUser);

        UUID organizationId = currentUser.getOrganization().getId();

        List<Property> properties = propertyRepository.findAvailableByOrganization(organizationId);

        log.debug("Se encontraron {} propiedades disponibles", properties.size());

        return properties.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PropertyResponse> getRentedProperties() {
        log.info("Obteniendo propiedades rentadas");

        User currentUser = getCurrentUser();
        validateUserHasOrganization(currentUser);

        UUID organizationId = currentUser.getOrganization().getId();

        List<Property> properties = propertyRepository.findRentedByOrganization(organizationId);

        log.debug("Se encontraron {} propiedades rentadas", properties.size());

        return properties.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }


    private User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email;

        if (principal instanceof UserDetails) {
            email = ((UserDetails) principal).getUsername();
        } else {
            email = principal.toString();
        }

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedAccessException("Usuario no autenticado"));
    }

    private void validateUserHasOrganization(User user) {
        if (user.getOrganization() == null) {
            log.warn("Usuario {} no tiene organización asignada", user.getEmail());
            throw new UnauthorizedAccessException("Debes pertenecer a una organización para realizar esta acción");
        }
    }

    private void validateUserCanAccessProperty(User user, Property property) {
        if (user.getOrganization() == null) {
            throw new UnauthorizedAccessException("Debes pertenecer a una organización");
        }

        if (!user.getOrganization().getId().equals(property.getOrganization().getId())) {
            log.warn("Usuario {} intentó acceder a propiedad {} de otra organización",
                    user.getEmail(), property.getId());
            throw new UnauthorizedAccessException("No tienes acceso a esta propiedad");
        }
    }

    private void validatePropertyType(String type) {
        if (!"CASA".equalsIgnoreCase(type) &&
                !"DEPARTAMENTO".equalsIgnoreCase(type) &&
                !"LOCAL_COMERCIAL".equalsIgnoreCase(type)) {
            throw new IllegalArgumentException(
                    "Tipo de propiedad inválido. Debe ser: CASA, DEPARTAMENTO o LOCAL_COMERCIAL"
            );
        }
    }

    private void validatePropertyStatus(String status) {
        if (!"DISPONIBLE".equalsIgnoreCase(status) &&
                !"RENTADA".equalsIgnoreCase(status) &&
                !"MANTENIMIENTO".equalsIgnoreCase(status)) {
            throw new IllegalArgumentException(
                    "Estado de propiedad inválido. Debe ser: DISPONIBLE, RENTADA o MANTENIMIENTO"
            );
        }
    }

    private void processPropertyImages(Property property, List<String> imageUrls, Organization organization) {
        if (!organization.canUploadImages()) {
            log.warn("Plan {} no permite subir imágenes", organization.getPlanCode());
            throw new OrganizationPropertyLimitException(
                    "Tu plan " + organization.getPlanCode() + " no permite subir imágenes. " +
                            "Por favor, mejora tu plan para habilitar esta funcionalidad."
            );
        }

        int maxImages = organization.getImagesPerPropertyLimit();

        if (imageUrls.size() > maxImages) {
            log.warn("Intento de subir {} imágenes cuando el plan {} permite máximo {}",
                    imageUrls.size(), organization.getPlanCode(), maxImages);
            throw new OrganizationPropertyLimitException(
                    "Has excedido el límite de " + maxImages + " imágenes por propiedad " +
                            "según tu plan " + organization.getPlanCode() + ". " +
                            "Por favor, mejora tu plan para subir más imágenes."
            );
        }

        for (int i = 0; i < imageUrls.size(); i++) {
            String imageUrl = imageUrls.get(i);
            PropertyImage propertyImage = PropertyImage.builder()
                    .property(property)
                    .imageUrl(imageUrl)
                    .imagePublicId(extractPublicIdFromUrl(imageUrl))
                    .displayOrder(i)
                    .isMain(i == 0)
                    .createdBy(getCurrentUser().getId())
                    .build();

            property.addImage(propertyImage);
        }
    }


    private void updatePropertyImages(Property property, List<String> newImageUrls, Organization organization) {
        if (!organization.canUploadImages()) {
            throw new OrganizationPropertyLimitException(
                    "Tu plan " + organization.getPlanCode() + " no permite subir imágenes. " +
                            "Por favor, mejora tu plan para habilitar esta funcionalidad."
            );
        }

        int maxImages = organization.getImagesPerPropertyLimit();

        if (newImageUrls.size() > maxImages) {
            throw new OrganizationPropertyLimitException(
                    "Has excedido el límite de " + maxImages + " imágenes por propiedad " +
                            "según tu plan " + organization.getPlanCode()
            );
        }

        // Obtener imágenes actuales
        List<PropertyImage> currentImages = new ArrayList<>(property.getImages());

        // Identificar y eliminar imágenes que ya no están en la nueva lista
        for (PropertyImage currentImage : currentImages) {
            if (!newImageUrls.contains(currentImage.getImageUrl())) {
                // Eliminar de Cloudinary si tiene publicId
                deleteImageFromCloudinary(currentImage.getImageUrl());

                // Eliminar de la propiedad
                property.removeImage(currentImage);
            }
        }

        // Agregar nuevas imágenes
        List<String> existingUrls = property.getImages().stream()
                .map(PropertyImage::getImageUrl)
                .collect(Collectors.toList());

        for (int i = 0; i < newImageUrls.size(); i++) {
            String imageUrl = newImageUrls.get(i);

            if (!existingUrls.contains(imageUrl)) {
                PropertyImage newImage = PropertyImage.builder()
                        .property(property)
                        .imageUrl(imageUrl)
                        .imagePublicId(extractPublicIdFromUrl(imageUrl))
                        .displayOrder(i)
                        .isMain(i == 0 && property.getImages().isEmpty())
                        .createdBy(getCurrentUser().getId())
                        .build();

                property.addImage(newImage);
            }
        }

        // Actualizar orden de visualización de todas las imágenes
        List<PropertyImage> sortedImages = property.getImages().stream()
                .sorted((a, b) -> {
                    int indexA = newImageUrls.indexOf(a.getImageUrl());
                    int indexB = newImageUrls.indexOf(b.getImageUrl());
                    return Integer.compare(indexA, indexB);
                })
                .collect(Collectors.toList());

        for (int i = 0; i < sortedImages.size(); i++) {
            sortedImages.get(i).setDisplayOrder(i);
            sortedImages.get(i).setIsMain(i == 0);
        }
    }

    private void deleteImageFromCloudinary(String imageUrl) {
        try {
            String publicId = extractPublicIdFromUrl(imageUrl);
            if (publicId != null && !publicId.isEmpty()) {
                cloudinaryService.deleteImage(publicId);
                log.info("Deleted image from Cloudinary - publicId: {}", publicId);
            }
        } catch (Exception e) {
            log.warn("Failed to delete image from Cloudinary - url: {}, error: {}",
                    imageUrl, e.getMessage());
        }
    }

    private String extractPublicIdFromUrl(String imageUrl) {
        if (imageUrl == null || !imageUrl.contains("cloudinary.com")) {
            return null;
        }

        try {
            String[] parts = imageUrl.split("/upload/");
            if (parts.length < 2) return null;

            String afterUpload = parts[1].replaceFirst("v\\d+/", "");
            int lastDot = afterUpload.lastIndexOf('.');
            return lastDot > 0 ? afterUpload.substring(0, lastDot) : afterUpload;
        } catch (Exception e) {
            log.warn("Error extracting public ID from URL: {}", imageUrl);
            return null;
        }
    }

    private PropertyResponse mapToResponse(Property property) {
        PropertyImage mainImage = property.getMainImage();
        return PropertyResponse.builder()
                .id(property.getId())
                .propertyCode(property.getPropertyCode())
                .propertyType(property.getPropertyType())
                .address(property.getAddress())
                .monthlyRent(property.getMonthlyRent())
                .waterFee(property.getWaterFee())
                .status(property.getStatus())
                .locationId(property.getLocation() != null ? property.getLocation().getId() : null)
                .locationName(property.getLocation() != null ? property.getLocation().getName() : null)
                .bedrooms(property.getBedrooms())
                .bathrooms(property.getBathrooms())
                .parkingSpaces(property.getParkingSpaces())
                .totalAreaM2(property.getTotalAreaM2())
                .isActive(property.getIsActive())
                .createdAt(property.getCreatedAt())
                .mainImageUrl(mainImage != null ? mainImage.getImageUrl() : null)
                .build();
    }

    private PropertyDetailResponse mapToDetailResponse(Property property) {
        PropertyDetailResponse.LocationDto locationDto = null;
        if (property.getLocation() != null) {
            Location loc = property.getLocation();
            locationDto = PropertyDetailResponse.LocationDto.builder()
                    .id(loc.getId())
                    .name(loc.getName())
                    .city(loc.getCity())
                    .state(loc.getState())
                    .build();
        }

        return PropertyDetailResponse.builder()
                .id(property.getId())
                .organizationId(property.getOrganization().getId())
                .organizationName(property.getOrganization().getName())
                .location(locationDto)
                .propertyCode(property.getPropertyCode())
                .propertyType(property.getPropertyType())
                .address(property.getAddress())
                .monthlyRent(property.getMonthlyRent())
                .waterFee(property.getWaterFee())
                .status(property.getStatus())
                .floors(property.getFloors())
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
                .isActive(property.getIsActive())
                .createdAt(property.getCreatedAt())
                .updatedAt(property.getUpdatedAt())
                .createdBy(property.getCreatedBy())
                .imageUrls(property.getImages().stream()
                        .sorted((a, b) -> Integer.compare(a.getDisplayOrder(), b.getDisplayOrder()))
                        .map(PropertyImage::getImageUrl)
                        .collect(Collectors.toList()))
                .build();
    }
}