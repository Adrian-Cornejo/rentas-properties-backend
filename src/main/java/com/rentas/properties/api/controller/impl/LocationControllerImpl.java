package com.rentas.properties.api.controller.impl;

import com.rentas.properties.api.controller.LocationController;
import com.rentas.properties.api.dto.request.CreateLocationRequest;
import com.rentas.properties.api.dto.request.UpdateLocationRequest;
import com.rentas.properties.api.dto.response.LocationDetailResponse;
import com.rentas.properties.api.dto.response.LocationResponse;
import com.rentas.properties.business.services.LocationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/locations")
@RequiredArgsConstructor
@Slf4j
public class LocationControllerImpl implements LocationController {

    private final LocationService locationService;

    @Override
    @PostMapping
    public ResponseEntity<LocationDetailResponse> createLocation(@Valid @RequestBody CreateLocationRequest request) {
        log.info("Creando nueva ubicación: {}", request.getName());
        LocationDetailResponse response = locationService.createLocation(request);
        log.info("Ubicación creada exitosamente con ID: {}", response.getId());
        return ResponseEntity.ok(response);
    }

    @Override
    @GetMapping
    public ResponseEntity<List<LocationResponse>> getAllLocations(
            @RequestParam(required = false, defaultValue = "false") boolean includeInactive
    ) {
        log.info("Obteniendo todas las ubicaciones - includeInactive: {}", includeInactive);
        List<LocationResponse> locations = locationService.getAllLocations(includeInactive);
        log.info("Se encontraron {} ubicaciones", locations.size());
        return ResponseEntity.ok(locations);
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<LocationDetailResponse> getLocationById(@PathVariable UUID id) {
        log.info("Obteniendo ubicación con ID: {}", id);
        LocationDetailResponse response = locationService.getLocationById(id);
        log.info("Ubicación encontrada: {}", response.getName());
        return ResponseEntity.ok(response);
    }

    @Override
    @PutMapping("/{id}")
    public ResponseEntity<LocationDetailResponse> updateLocation(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateLocationRequest request
    ) {
        log.info("Actualizando ubicación con ID: {}", id);
        LocationDetailResponse response = locationService.updateLocation(id, request);
        log.info("Ubicación actualizada exitosamente: {}", response.getName());
        return ResponseEntity.ok(response);
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLocation(@PathVariable UUID id) {
        log.info("Eliminando ubicación con ID: {}", id);
        locationService.deleteLocation(id);
        log.info("Ubicación eliminada exitosamente");
        return ResponseEntity.ok().build();
    }

    @Override
    @GetMapping("/by-city")
    public ResponseEntity<List<LocationResponse>> getLocationsByCity(@RequestParam String city) {
        log.info("Obteniendo ubicaciones por ciudad: {}", city);
        List<LocationResponse> locations = locationService.getLocationsByCity(city);
        log.info("Se encontraron {} ubicaciones en {}", locations.size(), city);
        return ResponseEntity.ok(locations);
    }

    @Override
    @GetMapping("/active")
    public ResponseEntity<List<LocationResponse>> getActiveLocations() {
        log.info("Obteniendo ubicaciones activas");
        List<LocationResponse> locations = locationService.getActiveLocations();
        log.info("Se encontraron {} ubicaciones activas", locations.size());
        return ResponseEntity.ok(locations);
    }
}