package com.rentas.properties.business.services.impl;

import com.rentas.properties.api.dto.request.CreateLocationRequest;
import com.rentas.properties.api.dto.request.UpdateLocationRequest;
import com.rentas.properties.api.dto.response.LocationResponse;
import com.rentas.properties.api.exception.LocationAlreadyExistsException;
import com.rentas.properties.api.exception.LocationHasPropertiesException;
import com.rentas.properties.api.exception.LocationNotFoundException;
import com.rentas.properties.business.services.LocationService;
import com.rentas.properties.dao.entity.Location;
import com.rentas.properties.dao.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LocationServiceImpl implements LocationService {

    private final LocationRepository locationRepository;

    @Override
    @Transactional(readOnly = true)
    public List<LocationResponse> findAll(boolean includeInactive) {
        log.info("Obteniendo todas las ubicaciones - includeInactive: {}", includeInactive);

        List<Location> locations;
        if (includeInactive) {
            locations = locationRepository.findAll();
        } else {
            locations = locationRepository.findByIsActiveTrue();
        }

        return locations.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public LocationResponse findById(UUID id) {
        log.info("Buscando ubicación con ID: {}", id);

        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new LocationNotFoundException(
                        "Ubicación no encontrada con ID: " + id
                ));

        return convertToResponse(location);
    }

    @Override
    @Transactional
    public LocationResponse create(CreateLocationRequest request) {
        log.info("Creando nueva ubicación: {}", request.getName());

        // Validar que no exista una ubicación con el mismo nombre
        if (locationRepository.existsByName(request.getName())) {
            throw new LocationAlreadyExistsException(
                    "Ya existe una ubicación con el nombre: " + request.getName()
            );
        }

        Location location = Location.builder()
                .name(request.getName())
                .address(request.getAddress())
                .city(request.getCity())
                .state(request.getState())
                .postalCode(request.getPostalCode())
                .description(request.getDescription())
                .isActive(true)
                .build();

        Location savedLocation = locationRepository.save(location);
        log.info("Ubicación creada exitosamente con ID: {}", savedLocation.getId());

        return convertToResponse(savedLocation);
    }

    @Override
    @Transactional
    public LocationResponse update(UUID id, UpdateLocationRequest request) {
        log.info("Actualizando ubicación con ID: {}", id);

        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new LocationNotFoundException(
                        "Ubicación no encontrada con ID: " + id
                ));

        // Validar nombre único si se está cambiando
        if (request.getName() != null &&
                !request.getName().equals(location.getName()) &&
                locationRepository.existsByName(request.getName())) {
            throw new LocationAlreadyExistsException(
                    "Ya existe una ubicación con el nombre: " + request.getName()
            );
        }

        // Actualizar campos solo si vienen en el request
        if (request.getName() != null) {
            location.setName(request.getName());
        }
        if (request.getAddress() != null) {
            location.setAddress(request.getAddress());
        }
        if (request.getCity() != null) {
            location.setCity(request.getCity());
        }
        if (request.getState() != null) {
            location.setState(request.getState());
        }
        if (request.getPostalCode() != null) {
            location.setPostalCode(request.getPostalCode());
        }
        if (request.getDescription() != null) {
            location.setDescription(request.getDescription());
        }
        if (request.getIsActive() != null) {
            location.setIsActive(request.getIsActive());
        }

        Location updatedLocation = locationRepository.save(location);
        log.info("Ubicación actualizada exitosamente: {}", updatedLocation.getName());

        return convertToResponse(updatedLocation);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        log.info("Eliminando ubicación con ID: {}", id);

        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new LocationNotFoundException(
                        "Ubicación no encontrada con ID: " + id
                ));

        // Validar que no tenga propiedades asociadas activas
        validateLocationCanBeDeleted(id);

        // Soft delete
        location.setIsActive(false);
        locationRepository.save(location);

        log.info("Ubicación desactivada exitosamente: {}", location.getName());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LocationResponse> findByCity(String city) {
        log.info("Buscando ubicaciones en la ciudad: {}", city);

        List<Location> locations = locationRepository.findByCity(city);

        return locations.stream()
                .filter(Location::getIsActive)
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByName(String name) {
        return locationRepository.existsByName(name);
    }

    @Override
    @Transactional(readOnly = true)
    public void validateLocationCanBeDeleted(UUID id) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new LocationNotFoundException(
                        "Ubicación no encontrada con ID: " + id
                ));

        // Verificar si tiene propiedades activas
        long activeProperties = location.getProperties().stream()
                .filter(p -> Boolean.TRUE.equals(p.getIsActive()))
                .count();

        if (activeProperties > 0) {
            throw new LocationHasPropertiesException(
                    String.format("La ubicación '%s' tiene %d propiedades activas y no puede ser eliminada",
                            location.getName(), activeProperties)
            );
        }
    }

    private LocationResponse convertToResponse(Location location) {
        return LocationResponse.builder()
                .id(location.getId())
                .name(location.getName())
                .address(location.getAddress())
                .city(location.getCity())
                .state(location.getState())
                .postalCode(location.getPostalCode())
                .description(location.getDescription())
                .isActive(location.getIsActive())
                .totalProperties(location.getTotalProperties())
                .availableProperties(location.getAvailablePropertiesCount())
                .fullAddress(location.getFullAddress())
                .createdAt(location.getCreatedAt())
                .updatedAt(location.getUpdatedAt())
                .createdBy(location.getCreatedBy())
                .build();
    }
}