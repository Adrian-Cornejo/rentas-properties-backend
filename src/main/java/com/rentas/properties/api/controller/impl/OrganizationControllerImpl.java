package com.rentas.properties.api.controller.impl;

import com.rentas.properties.api.controller.OrganizationController;
import com.rentas.properties.api.dto.request.CreateOrganizationRequest;
import com.rentas.properties.api.dto.request.UpdateOrganizationRequest;
import com.rentas.properties.api.dto.response.OrganizationDetailResponse;
import com.rentas.properties.api.dto.response.OrganizationInfoResponse;
import com.rentas.properties.api.dto.response.OrganizationResponse;
import com.rentas.properties.api.dto.response.OrganizationStatsResponse;
import com.rentas.properties.business.services.OrganizationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/organizations")
@RequiredArgsConstructor
@Slf4j
public class OrganizationControllerImpl implements OrganizationController {

    private final OrganizationService organizationService;

    @Override
    @PostMapping
    public ResponseEntity<OrganizationDetailResponse> createOrganization(@Valid @RequestBody CreateOrganizationRequest request) {
        log.info("Creando nueva organización: {}", request.getName());
        OrganizationDetailResponse response = organizationService.createOrganization(request);
        log.info("Organización creada exitosamente con ID: {}", response.getId());
        return ResponseEntity.ok(response);
    }

    @Override
    @GetMapping
    public ResponseEntity<List<OrganizationResponse>> getAllOrganizations() {
        log.info("Obteniendo todas las organizaciones");
        List<OrganizationResponse> organizations = organizationService.getAllOrganizations();
        log.info("Se encontraron {} organizaciones", organizations.size());
        return ResponseEntity.ok(organizations);
    }

    @Override
    @GetMapping("/me")
    public ResponseEntity<OrganizationDetailResponse> getMyOrganization() {
        log.info("GET /api/v1/organizations/me - Obteniendo organización del usuario autenticado");

        return organizationService.getMyOrganization()
                .map(response -> {
                    log.info("Organización encontrada: {}", response.getName());
                    return ResponseEntity.ok(response);
                })
                .orElseGet(() -> {
                    log.info("Usuario no tiene organización asignada - Retornando 404");
                    return ResponseEntity.notFound().build();
                });
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<OrganizationDetailResponse> getOrganizationById(@PathVariable UUID id) {
        log.info("Obteniendo organización con ID: {}", id);
        OrganizationDetailResponse response = organizationService.getOrganizationById(id);
        log.info("Organización encontrada: {}", response.getName());
        return ResponseEntity.ok(response);
    }

    @Override
    @PutMapping("/{id}")
    public ResponseEntity<OrganizationDetailResponse> updateOrganization(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateOrganizationRequest request
    ) {
        log.info("Actualizando organización con ID: {}", id);
        OrganizationDetailResponse response = organizationService.updateOrganization(id, request);
        log.info("Organización actualizada exitosamente: {}", response.getName());
        return ResponseEntity.ok(response);
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrganization(@PathVariable UUID id) {
        log.info("Eliminando organización con ID: {}", id);
        organizationService.deleteOrganization(id);
        log.info("Organización eliminada exitosamente");
        return ResponseEntity.ok().build();
    }

    @Override
    @PostMapping("/{id}/regenerate-code")
    public ResponseEntity<OrganizationDetailResponse> regenerateInvitationCode(@PathVariable UUID id) {
        log.info("Regenerando código de invitación para organización: {}", id);
        OrganizationDetailResponse response = organizationService.regenerateInvitationCode(id);
        log.info("Código regenerado exitosamente: {}", response.getInvitationCode());
        return ResponseEntity.ok(response);
    }

    @Override
    @GetMapping("/validate-code")
    public ResponseEntity<OrganizationResponse> validateInvitationCode(@RequestParam String code) {
        log.info("Validando código de invitación: {}", code);
        OrganizationResponse response = organizationService.validateInvitationCode(code);
        log.info("Código válido para organización: {}", response.getName());
        return ResponseEntity.ok(response);
    }

    @Override
    @GetMapping("/{id}/stats")
    public ResponseEntity<OrganizationStatsResponse> getOrganizationStats(@PathVariable UUID id) {
        log.info("Obteniendo estadísticas de organización: {}", id);
        OrganizationStatsResponse stats = organizationService.getOrganizationStats(id);
        log.info("Estadísticas obtenidas exitosamente");
        return ResponseEntity.ok(stats);
    }

    @Override
    @GetMapping("/me/stats")
    public ResponseEntity<OrganizationStatsResponse> getMyOrganizationStats() {
        log.info("GET /api/v1/organizations/me/stats - Obteniendo estadísticas de mi organización");
        OrganizationStatsResponse stats = organizationService.getMyOrganizationStats();
        log.info("Estadísticas obtenidas exitosamente");
        return ResponseEntity.ok(stats);
    }

    @Override
    @GetMapping("/active")
    public ResponseEntity<List<OrganizationResponse>> getActiveOrganizations() {
        log.info("Obteniendo organizaciones activas");
        List<OrganizationResponse> organizations = organizationService.getActiveOrganizations();
        log.info("Se encontraron {} organizaciones activas", organizations.size());
        return ResponseEntity.ok(organizations);
    }

    @Override
    @GetMapping("/me/info")
    public ResponseEntity<OrganizationInfoResponse> getMyOrganizationInfo() {
        log.info("Obteniendo información básica de organización del usuario");
        OrganizationInfoResponse response = organizationService.getMyOrganizationInfo();
        return ResponseEntity.ok(response);
    }
}