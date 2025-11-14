package com.rentas.properties.business.services.impl;

import com.rentas.properties.api.dto.request.CreateLocationRequest;
import com.rentas.properties.api.dto.request.UpdateLocationRequest;
import com.rentas.properties.api.dto.response.LocationDetailResponse;
import com.rentas.properties.api.dto.response.LocationResponse;
import com.rentas.properties.api.exception.LocationAlreadyExistsException;
import com.rentas.properties.api.exception.LocationHasPropertiesException;
import com.rentas.properties.api.exception.LocationNotFoundException;
import com.rentas.properties.api.exception.UnauthorizedAccessException;
import com.rentas.properties.business.services.LocationService;
import com.rentas.properties.dao.entity.Location;
import com.rentas.properties.dao.entity.Organization;
import com.rentas.properties.dao.entity.User;
import com.rentas.properties.dao.repository.LocationRepository;
import com.rentas.properties.dao.repository.PropertyRepository;
import com.rentas.properties.dao.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
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
    private final UserRepository userRepository;
    private final PropertyRepository propertyRepository;

    @Override
    @Transactional
    public LocationDetailResponse createLocation(CreateLocationRequest request) {
        log.info("Creando ubicación: {}", request.getName());

        User currentUser = getCurrentUser();
        validateUserHasOrganization(currentUser);

        Organization organization = currentUser.getOrganization();

        if (locationRepository.existsByNameAndOrganization_Id(request.getName(), organization.getId())) {
            log.warn("Ya existe una ubicación con el nombre '{}' en la organización {}",
                    request.getName(), organization.getId());
            throw new LocationAlreadyExistsException(
                    "Ya existe una ubicación con el nombre '" + request.getName() + "' en tu organización"
            );
        }

        Location location = Location.builder()
                .organization(organization)
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

        return mapToDetailResponse(savedLocation);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LocationResponse> getAllLocations(boolean includeInactive) {
        log.info("Obteniendo todas las ubicaciones - includeInactive: {}", includeInactive);

        User currentUser = getCurrentUser();
        validateUserHasOrganization(currentUser);

        UUID organizationId = currentUser.getOrganization().getId();

        List<Location> locations;
        if (includeInactive) {
            locations = locationRepository.findByOrganization_Id(organizationId);
        } else {
            locations = locationRepository.findByOrganization_IdAndIsActiveTrue(organizationId);
        }

        log.debug("Se encontraron {} ubicaciones", locations.size());

        return locations.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public LocationDetailResponse getLocationById(UUID id) {
        log.info("Obteniendo ubicación con ID: {}", id);

        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new LocationNotFoundException("Ubicación no encontrada con ID: " + id));

        User currentUser = getCurrentUser();
        validateUserCanAccessLocation(currentUser, location);

        Long propertyCount = propertyRepository.countActiveByOrganization_Id(location.getOrganization().getId());

        LocationDetailResponse response = mapToDetailResponse(location);
        response.setTotalProperties(propertyCount.intValue());

        return response;
    }

    @Override
    @Transactional
    public LocationDetailResponse updateLocation(UUID id, UpdateLocationRequest request) {
        log.info("Actualizando ubicación con ID: {}", id);

        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new LocationNotFoundException("Ubicación no encontrada con ID: " + id));

        User currentUser = getCurrentUser();
        validateUserCanAccessLocation(currentUser, location);

        if (request.getName() != null && !request.getName().equals(location.getName())) {
            if (locationRepository.existsByNameAndOrganization_Id(
                    request.getName(), location.getOrganization().getId())) {
                throw new LocationAlreadyExistsException(
                        "Ya existe una ubicación con el nombre '" + request.getName() + "' en tu organización"
                );
            }
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

        Location updatedLocation = locationRepository.save(location);
        log.info("Ubicación actualizada exitosamente: {}", updatedLocation.getName());

        return mapToDetailResponse(updatedLocation);
    }

    @Override
    @Transactional
    public void deleteLocation(UUID id) {
        log.info("Eliminando ubicación con ID: {}", id);

        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new LocationNotFoundException("Ubicación no encontrada con ID: " + id));

        User currentUser = getCurrentUser();
        validateUserCanAccessLocation(currentUser, location);

        Long propertyCount = propertyRepository.countActiveByOrganization_Id(location.getOrganization().getId());
        if (propertyCount > 0) {
            log.warn("No se puede eliminar la ubicación {} porque tiene {} propiedades asociadas",
                    id, propertyCount);
            throw new LocationHasPropertiesException(
                    "No se puede eliminar la ubicación porque tiene " + propertyCount + " propiedades asociadas"
            );
        }

        location.setIsActive(false);
        locationRepository.save(location);

        log.info("Ubicación desactivada exitosamente: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LocationResponse> getLocationsByCity(String city) {
        log.info("Obteniendo ubicaciones por ciudad: {}", city);

        User currentUser = getCurrentUser();
        validateUserHasOrganization(currentUser);

        UUID organizationId = currentUser.getOrganization().getId();

        List<Location> locations = locationRepository.findByOrganization_Id(organizationId)
                .stream()
                .filter(loc -> city.equalsIgnoreCase(loc.getCity()))
                .collect(Collectors.toList());

        log.debug("Se encontraron {} ubicaciones en {}", locations.size(), city);

        return locations.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LocationResponse> getActiveLocations() {
        log.info("Obteniendo ubicaciones activas");

        User currentUser = getCurrentUser();
        validateUserHasOrganization(currentUser);

        UUID organizationId = currentUser.getOrganization().getId();

        List<Location> locations = locationRepository.findByOrganization_IdAndIsActiveTrue(organizationId);

        log.debug("Se encontraron {} ubicaciones activas", locations.size());

        return locations.stream()
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

    private void validateUserCanAccessLocation(User user, Location location) {
        if (user.getOrganization() == null) {
            throw new UnauthorizedAccessException("Debes pertenecer a una organización");
        }

        if (!user.getOrganization().getId().equals(location.getOrganization().getId())) {
            log.warn("Usuario {} intentó acceder a ubicación {} de otra organización",
                    user.getEmail(), location.getId());
            throw new UnauthorizedAccessException("No tienes acceso a esta ubicación");
        }
    }

    private LocationResponse mapToResponse(Location location) {
        return LocationResponse.builder()
                .id(location.getId())
                .name(location.getName())
                .address(location.getAddress())
                .city(location.getCity())
                .state(location.getState())
                .postalCode(location.getPostalCode())
                .description(location.getDescription())
                .isActive(location.getIsActive())
                .createdAt(location.getCreatedAt())
                .build();
    }

    private LocationDetailResponse mapToDetailResponse(Location location) {
        return LocationDetailResponse.builder()
                .id(location.getId())
                .organizationId(location.getOrganization().getId())
                .organizationName(location.getOrganization().getName())
                .name(location.getName())
                .address(location.getAddress())
                .city(location.getCity())
                .state(location.getState())
                .postalCode(location.getPostalCode())
                .description(location.getDescription())
                .isActive(location.getIsActive())
                .createdAt(location.getCreatedAt())
                .updatedAt(location.getUpdatedAt())
                .createdBy(location.getCreatedBy())
                .build();
    }
}