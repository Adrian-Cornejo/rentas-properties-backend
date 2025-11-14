package com.rentas.properties.api.controller.impl;

import com.rentas.properties.api.controller.TenantController;
import com.rentas.properties.api.dto.request.CreateTenantRequest;
import com.rentas.properties.api.dto.request.UpdateTenantRequest;
import com.rentas.properties.api.dto.response.TenantDetailResponse;
import com.rentas.properties.api.dto.response.TenantResponse;
import com.rentas.properties.business.services.TenantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tenants")
@RequiredArgsConstructor
@Slf4j
public class TenantControllerImpl implements TenantController {

    private final TenantService tenantService;

    @Override
    @PostMapping
    public ResponseEntity<TenantDetailResponse> createTenant(@Valid @RequestBody CreateTenantRequest request) {
        log.info("Creando nuevo arrendatario: {}", request.getFullName());
        TenantDetailResponse response = tenantService.createTenant(request);
        log.info("Arrendatario creado exitosamente con ID: {}", response.getId());
        return ResponseEntity.ok(response);
    }

    @Override
    @GetMapping
    public ResponseEntity<List<TenantResponse>> getAllTenants(
            @RequestParam(required = false, defaultValue = "false") boolean includeInactive
    ) {
        log.info("Obteniendo todos los arrendatarios - includeInactive: {}", includeInactive);
        List<TenantResponse> tenants = tenantService.getAllTenants(includeInactive);
        log.info("Se encontraron {} arrendatarios", tenants.size());
        return ResponseEntity.ok(tenants);
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<TenantDetailResponse> getTenantById(@PathVariable UUID id) {
        log.info("Obteniendo arrendatario con ID: {}", id);
        TenantDetailResponse response = tenantService.getTenantById(id);
        log.info("Arrendatario encontrado: {}", response.getFullName());
        return ResponseEntity.ok(response);
    }

    @Override
    @PutMapping("/{id}")
    public ResponseEntity<TenantDetailResponse> updateTenant(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTenantRequest request
    ) {
        log.info("Actualizando arrendatario con ID: {}", id);
        TenantDetailResponse response = tenantService.updateTenant(id, request);
        log.info("Arrendatario actualizado exitosamente: {}", response.getFullName());
        return ResponseEntity.ok(response);
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTenant(@PathVariable UUID id) {
        log.info("Eliminando arrendatario con ID: {}", id);
        tenantService.deleteTenant(id);
        log.info("Arrendatario eliminado exitosamente");
        return ResponseEntity.ok().build();
    }

    @Override
    @GetMapping("/search")
    public ResponseEntity<List<TenantResponse>> searchTenantsByName(@RequestParam String name) {
        log.info("Buscando arrendatarios por nombre: {}", name);
        List<TenantResponse> tenants = tenantService.searchTenantsByName(name);
        log.info("Se encontraron {} arrendatarios", tenants.size());
        return ResponseEntity.ok(tenants);
    }

    @Override
    @GetMapping("/by-phone")
    public ResponseEntity<TenantDetailResponse> getTenantByPhone(@RequestParam String phone) {
        log.info("Buscando arrendatario por teléfono: {}", phone);
        TenantDetailResponse response = tenantService.getTenantByPhone(phone);
        log.info("Arrendatario encontrado: {}", response.getFullName());
        return ResponseEntity.ok(response);
    }

    @Override
    @GetMapping("/active")
    public ResponseEntity<List<TenantResponse>> getActiveTenants() {
        log.info("Obteniendo arrendatarios activos");
        List<TenantResponse> tenants = tenantService.getActiveTenants();
        log.info("Se encontraron {} arrendatarios activos", tenants.size());
        return ResponseEntity.ok(tenants);
    }
}