package com.rentas.properties.api.controller.impl;

import com.rentas.properties.api.controller.MaintenanceRecordController;
import com.rentas.properties.api.dto.request.CreateMaintenanceRecordRequest;
import com.rentas.properties.api.dto.request.UpdateMaintenanceRecordRequest;
import com.rentas.properties.api.dto.response.MaintenanceRecordDetailResponse;
import com.rentas.properties.api.dto.response.MaintenanceRecordResponse;
import com.rentas.properties.api.dto.response.MaintenanceRecordSummaryResponse;
import com.rentas.properties.business.services.MaintenanceRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/maintenance-records")
@RequiredArgsConstructor
@Slf4j
public class MaintenanceRecordControllerImpl implements MaintenanceRecordController {

    private final MaintenanceRecordService maintenanceRecordService;

    @Override
    @PostMapping
    public ResponseEntity<MaintenanceRecordDetailResponse> createMaintenanceRecord(
            @Valid @RequestBody CreateMaintenanceRecordRequest request
    ) {
        log.info("Creando registro de mantenimiento para propiedad ID: {}", request.getPropertyId());
        MaintenanceRecordDetailResponse response = maintenanceRecordService.createMaintenanceRecord(request);
        log.info("Registro de mantenimiento creado exitosamente con ID: {}", response.getId());
        return ResponseEntity.ok(response);
    }

    @Override
    @GetMapping
    public ResponseEntity<List<MaintenanceRecordResponse>> getAllMaintenanceRecords() {
        log.info("Obteniendo todos los registros de mantenimiento");
        List<MaintenanceRecordResponse> records = maintenanceRecordService.getAllMaintenanceRecords();
        log.info("Se encontraron {} registros de mantenimiento", records.size());
        return ResponseEntity.ok(records);
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<MaintenanceRecordDetailResponse> getMaintenanceRecordById(@PathVariable UUID id) {
        log.info("Obteniendo registro de mantenimiento con ID: {}", id);
        MaintenanceRecordDetailResponse response = maintenanceRecordService.getMaintenanceRecordById(id);
        log.info("Registro encontrado: {}", response.getTitle());
        return ResponseEntity.ok(response);
    }

    @Override
    @PutMapping("/{id}")
    public ResponseEntity<MaintenanceRecordDetailResponse> updateMaintenanceRecord(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateMaintenanceRecordRequest request
    ) {
        log.info("Actualizando registro de mantenimiento con ID: {}", id);
        MaintenanceRecordDetailResponse response = maintenanceRecordService.updateMaintenanceRecord(id, request);
        log.info("Registro de mantenimiento actualizado exitosamente");
        return ResponseEntity.ok(response);
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMaintenanceRecord(@PathVariable UUID id) {
        log.info("Eliminando registro de mantenimiento con ID: {}", id);
        maintenanceRecordService.deleteMaintenanceRecord(id);
        log.info("Registro de mantenimiento eliminado exitosamente");
        return ResponseEntity.ok().build();
    }

    @Override
    @PostMapping("/{id}/images")
    public ResponseEntity<MaintenanceRecordDetailResponse> addImage(
            @PathVariable UUID id,
            @RequestParam String imageUrl,
            @RequestParam String imagePublicId,
            @RequestParam String imageType,
            @RequestParam(required = false) String description
    ) {
        log.info("Agregando imagen tipo {} al registro {}", imageType, id);
        MaintenanceRecordDetailResponse response = maintenanceRecordService.addImage(
                id, imageUrl, imagePublicId, imageType, description);
        log.info("Imagen agregada exitosamente");
        return ResponseEntity.ok(response);
    }

    @Override
    @DeleteMapping("/images/{imageId}")
    public ResponseEntity<Void> deleteImage(@PathVariable UUID imageId) {
        log.info("Eliminando imagen con ID: {}", imageId);
        maintenanceRecordService.deleteImage(imageId);
        log.info("Imagen eliminada exitosamente");
        return ResponseEntity.ok().build();
    }

    @Override
    @GetMapping("/property/{propertyId}")
    public ResponseEntity<List<MaintenanceRecordResponse>> getMaintenanceRecordsByProperty(
            @PathVariable UUID propertyId
    ) {
        log.info("Obteniendo registros de mantenimiento de la propiedad: {}", propertyId);
        List<MaintenanceRecordResponse> records = maintenanceRecordService.getMaintenanceRecordsByProperty(propertyId);
        log.info("Se encontraron {} registros para la propiedad", records.size());
        return ResponseEntity.ok(records);
    }

    @Override
    @GetMapping("/contract/{contractId}")
    public ResponseEntity<List<MaintenanceRecordResponse>> getMaintenanceRecordsByContract(
            @PathVariable UUID contractId
    ) {
        log.info("Obteniendo registros de mantenimiento del contrato: {}", contractId);
        List<MaintenanceRecordResponse> records = maintenanceRecordService.getMaintenanceRecordsByContract(contractId);
        log.info("Se encontraron {} registros para el contrato", records.size());
        return ResponseEntity.ok(records);
    }

    @Override
    @GetMapping("/by-status")
    public ResponseEntity<List<MaintenanceRecordResponse>> getMaintenanceRecordsByStatus(
            @RequestParam String status
    ) {
        log.info("Obteniendo registros por estado: {}", status);
        List<MaintenanceRecordResponse> records = maintenanceRecordService.getMaintenanceRecordsByStatus(status);
        log.info("Se encontraron {} registros con estado {}", records.size(), status);
        return ResponseEntity.ok(records);
    }

    @Override
    @GetMapping("/by-type")
    public ResponseEntity<List<MaintenanceRecordResponse>> getMaintenanceRecordsByType(
            @RequestParam String type
    ) {
        log.info("Obteniendo registros por tipo: {}", type);
        List<MaintenanceRecordResponse> records = maintenanceRecordService.getMaintenanceRecordsByType(type);
        log.info("Se encontraron {} registros de tipo {}", records.size(), type);
        return ResponseEntity.ok(records);
    }

    @Override
    @GetMapping("/by-category")
    public ResponseEntity<List<MaintenanceRecordResponse>> getMaintenanceRecordsByCategory(
            @RequestParam String category
    ) {
        log.info("Obteniendo registros por categoría: {}", category);
        List<MaintenanceRecordResponse> records = maintenanceRecordService.getMaintenanceRecordsByCategory(category);
        log.info("Se encontraron {} registros de categoría {}", records.size(), category);
        return ResponseEntity.ok(records);
    }

    @Override
    @GetMapping("/pending")
    public ResponseEntity<List<MaintenanceRecordResponse>> getPendingMaintenanceRecords() {
        log.info("Obteniendo registros pendientes");
        List<MaintenanceRecordResponse> records = maintenanceRecordService.getPendingMaintenanceRecords();
        log.info("Se encontraron {} registros pendientes", records.size());
        return ResponseEntity.ok(records);
    }

    @Override
    @PostMapping("/{id}/complete")
    public ResponseEntity<MaintenanceRecordDetailResponse> markAsCompleted(
            @PathVariable UUID id,
            @RequestParam(required = false) String actualCost
    ) {
        log.info("Marcando registro {} como completado", id);
        MaintenanceRecordDetailResponse response = maintenanceRecordService.markAsCompleted(id, actualCost);
        log.info("Registro marcado como COMPLETADO exitosamente");
        return ResponseEntity.ok(response);
    }

    @Override
    @GetMapping("/summary")
    public ResponseEntity<MaintenanceRecordSummaryResponse> getMaintenanceRecordsSummary() {
        log.info("Obteniendo resumen de registros de mantenimiento");
        MaintenanceRecordSummaryResponse response = maintenanceRecordService.getMaintenanceRecordsSummary();
        log.info("Resumen obtenido exitosamente");
        return ResponseEntity.ok(response);
    }
}
