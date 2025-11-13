package com.rentas.properties.api.controller.impl;

import com.rentas.properties.api.controller.LocationController;
import com.rentas.properties.api.dto.request.CreateLocationRequest;
import com.rentas.properties.api.dto.request.UpdateLocationRequest;
import com.rentas.properties.api.dto.response.LocationResponse;
import com.rentas.properties.business.services.LocationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @GetMapping
    public ResponseEntity<List<LocationResponse>> findAll(
            @RequestParam(required = false, defaultValue = "false") boolean includeInactive
    ) {
        log.info("Obteniendo todas las ubicaciones - includeInactive: {}", includeInactive);
        List<LocationResponse> locations = locationService.findAll(includeInactive);
        log.info("Se encontraron {} ubicaciones", locations.size());
        return ResponseEntity.ok(locations);
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<LocationResponse> findById(@PathVariable UUID id) {
        log.info("Buscando ubicación con ID: {}", id);
        LocationResponse location = locationService.findById(id);
        log.info("Ubicación encontrada: {}", location.getName());
        return ResponseEntity.ok(location);
    }

    @Override
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LocationResponse> create(@RequestBody CreateLocationRequest request) {
        log.info("Creando nueva ubicación: {}", request.getName());
        LocationResponse location = locationService.create(request);
        log.info("Ubicación creada exitosamente con ID: {}", location.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(location);
    }

    @Override
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LocationResponse> update(
            @PathVariable UUID id,
            @RequestBody UpdateLocationRequest request
    ) {
        log.info("Actualizando ubicación con ID: {}", id);
        LocationResponse location = locationService.update(id, request);
        log.info("Ubicación actualizada exitosamente: {}", location.getName());
        return ResponseEntity.ok(location);
    }

    @Override
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        log.info("Eliminando ubicación con ID: {}", id);
        locationService.delete(id);
        log.info("Ubicación eliminada exitosamente");
        return ResponseEntity.noContent().build();
    }

    @Override
    @GetMapping("/city/{city}")
    public ResponseEntity<List<LocationResponse>> findByCity(@PathVariable String city) {
        log.info("Buscando ubicaciones en la ciudad: {}", city);
        List<LocationResponse> locations = locationService.findByCity(city);
        log.info("Se encontraron {} ubicaciones en {}", locations.size(), city);
        return ResponseEntity.ok(locations);
    }
}