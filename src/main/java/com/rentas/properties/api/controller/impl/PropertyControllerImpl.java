package com.rentas.properties.api.controller.impl;

import com.rentas.properties.api.controller.PropertyController;
import com.rentas.properties.api.dto.request.CreatePropertyRequest;
import com.rentas.properties.api.dto.request.UpdatePropertyRequest;
import com.rentas.properties.api.dto.response.PropertyDetailResponse;
import com.rentas.properties.api.dto.response.PropertyResponse;
import com.rentas.properties.business.services.PropertyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/properties")
@RequiredArgsConstructor
@Slf4j
public class PropertyControllerImpl implements PropertyController {

    private final PropertyService propertyService;

    @Override
    @PostMapping
    public ResponseEntity<PropertyDetailResponse> createProperty(@Valid @RequestBody CreatePropertyRequest request) {
        log.info("Creando nueva propiedad con código: {}", request.getPropertyCode());
        PropertyDetailResponse response = propertyService.createProperty(request);
        log.info("Propiedad creada exitosamente con ID: {}", response.getId());
        return ResponseEntity.ok(response);
    }

    @Override
    @GetMapping
    public ResponseEntity<List<PropertyResponse>> getAllProperties(
            @RequestParam(required = false, defaultValue = "false") boolean includeInactive
    ) {
        log.info("Obteniendo todas las propiedades - includeInactive: {}", includeInactive);
        List<PropertyResponse> properties = propertyService.getAllProperties(includeInactive);
        log.info("Se encontraron {} propiedades", properties.size());
        return ResponseEntity.ok(properties);
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<PropertyDetailResponse> getPropertyById(@PathVariable UUID id) {
        log.info("Obteniendo propiedad con ID: {}", id);
        PropertyDetailResponse response = propertyService.getPropertyById(id);
        log.info("Propiedad encontrada: {}", response.getPropertyCode());
        return ResponseEntity.ok(response);
    }

    @Override
    @GetMapping("/by-code")
    public ResponseEntity<PropertyDetailResponse> getPropertyByCode(@RequestParam String code) {
        log.info("Buscando propiedad por código: {}", code);
        PropertyDetailResponse response = propertyService.getPropertyByCode(code);
        log.info("Propiedad encontrada: {}", response.getPropertyCode());
        return ResponseEntity.ok(response);
    }

    @Override
    @PutMapping("/{id}")
    public ResponseEntity<PropertyDetailResponse> updateProperty(
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePropertyRequest request
    ) {
        log.info("Actualizando propiedad con ID: {}", id);
        PropertyDetailResponse response = propertyService.updateProperty(id, request);
        log.info("Propiedad actualizada exitosamente: {}", response.getPropertyCode());
        return ResponseEntity.ok(response);
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProperty(@PathVariable UUID id) {
        log.info("Eliminando propiedad con ID: {}", id);
        propertyService.deleteProperty(id);
        log.info("Propiedad eliminada exitosamente");
        return ResponseEntity.ok().build();
    }

    @Override
    @GetMapping("/by-status")
    public ResponseEntity<List<PropertyResponse>> getPropertiesByStatus(@RequestParam String status) {
        log.info("Obteniendo propiedades por estado: {}", status);
        List<PropertyResponse> properties = propertyService.getPropertiesByStatus(status);
        log.info("Se encontraron {} propiedades con estado {}", properties.size(), status);
        return ResponseEntity.ok(properties);
    }

    @Override
    @GetMapping("/by-location/{locationId}")
    public ResponseEntity<List<PropertyResponse>> getPropertiesByLocation(@PathVariable UUID locationId) {
        log.info("Obteniendo propiedades de la ubicación: {}", locationId);
        List<PropertyResponse> properties = propertyService.getPropertiesByLocation(locationId);
        log.info("Se encontraron {} propiedades", properties.size());
        return ResponseEntity.ok(properties);
    }

    @Override
    @GetMapping("/by-type")
    public ResponseEntity<List<PropertyResponse>> getPropertiesByType(@RequestParam String type) {
        log.info("Obteniendo propiedades por tipo: {}", type);
        List<PropertyResponse> properties = propertyService.getPropertiesByType(type);
        log.info("Se encontraron {} propiedades de tipo {}", properties.size(), type);
        return ResponseEntity.ok(properties);
    }

    @Override
    @GetMapping("/available")
    public ResponseEntity<List<PropertyResponse>> getAvailableProperties() {
        log.info("Obteniendo propiedades disponibles");
        List<PropertyResponse> properties = propertyService.getAvailableProperties();
        log.info("Se encontraron {} propiedades disponibles", properties.size());
        return ResponseEntity.ok(properties);
    }

    @Override
    @GetMapping("/rented")
    public ResponseEntity<List<PropertyResponse>> getRentedProperties() {
        log.info("Obteniendo propiedades rentadas");
        List<PropertyResponse> properties = propertyService.getRentedProperties();
        log.info("Se encontraron {} propiedades rentadas", properties.size());
        return ResponseEntity.ok(properties);
    }
}