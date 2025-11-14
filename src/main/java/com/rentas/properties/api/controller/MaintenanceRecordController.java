package com.rentas.properties.api.controller;

import com.rentas.properties.api.dto.request.CreateMaintenanceRecordRequest;
import com.rentas.properties.api.dto.request.UpdateMaintenanceRecordRequest;
import com.rentas.properties.api.dto.response.MaintenanceRecordDetailResponse;
import com.rentas.properties.api.dto.response.MaintenanceRecordResponse;
import com.rentas.properties.api.dto.response.MaintenanceRecordSummaryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

@Tag(name = "Maintenance Records", description = "Endpoints para gestión de mantenimiento y reparaciones")
public interface MaintenanceRecordController {

    @Operation(
            summary = "Crear registro de mantenimiento",
            description = "Crea un nuevo registro de mantenimiento. El contractId es opcional (para propiedades disponibles)."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Registro creado exitosamente",
                    content = @Content(schema = @Schema(implementation = MaintenanceRecordDetailResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Propiedad o contrato no encontrado"),
            @ApiResponse(responseCode = "403", description = "No tienes permisos")
    })
    ResponseEntity<MaintenanceRecordDetailResponse> createMaintenanceRecord(
            @Valid @RequestBody CreateMaintenanceRecordRequest request
    );

    @Operation(
            summary = "Obtener todos los registros de mantenimiento",
            description = "Lista todos los registros de mantenimiento de la organización"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista obtenida exitosamente",
                    content = @Content(schema = @Schema(implementation = MaintenanceRecordResponse.class))
            )
    })
    ResponseEntity<List<MaintenanceRecordResponse>> getAllMaintenanceRecords();

    @Operation(
            summary = "Obtener registro por ID",
            description = "Obtiene los detalles de un registro de mantenimiento específico"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Registro encontrado",
                    content = @Content(schema = @Schema(implementation = MaintenanceRecordDetailResponse.class))
            ),
            @ApiResponse(responseCode = "404", description = "Registro no encontrado"),
            @ApiResponse(responseCode = "403", description = "No tienes acceso a este registro")
    })
    ResponseEntity<MaintenanceRecordDetailResponse> getMaintenanceRecordById(@PathVariable UUID id);

    @Operation(
            summary = "Actualizar registro de mantenimiento",
            description = "Actualiza la información de un registro de mantenimiento"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Registro actualizado exitosamente",
                    content = @Content(schema = @Schema(implementation = MaintenanceRecordDetailResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Registro no encontrado"),
            @ApiResponse(responseCode = "403", description = "No tienes permisos")
    })
    ResponseEntity<MaintenanceRecordDetailResponse> updateMaintenanceRecord(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateMaintenanceRecordRequest request
    );

    @Operation(
            summary = "Eliminar registro de mantenimiento",
            description = "Elimina un registro de mantenimiento (soft delete)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Registro eliminado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Registro no encontrado"),
            @ApiResponse(responseCode = "403", description = "No tienes permisos")
    })
    ResponseEntity<Void> deleteMaintenanceRecord(@PathVariable UUID id);

    @Operation(
            summary = "Agregar imagen al registro",
            description = "Agrega una imagen al registro (ANTES, DESPUES, EVIDENCIA)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Imagen agregada exitosamente",
                    content = @Content(schema = @Schema(implementation = MaintenanceRecordDetailResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Registro no encontrado")
    })
    ResponseEntity<MaintenanceRecordDetailResponse> addImage(
            @PathVariable UUID id,
            @RequestParam String imageUrl,
            @RequestParam String imagePublicId,
            @RequestParam String imageType,
            @RequestParam(required = false) String description
    );

    @Operation(
            summary = "Eliminar imagen del registro",
            description = "Elimina una imagen específica del registro de mantenimiento"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Imagen eliminada exitosamente"),
            @ApiResponse(responseCode = "404", description = "Imagen no encontrada")
    })
    ResponseEntity<Void> deleteImage(@PathVariable UUID imageId);

    @Operation(
            summary = "Obtener registros por propiedad",
            description = "Lista todos los registros de mantenimiento de una propiedad"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista obtenida exitosamente",
                    content = @Content(schema = @Schema(implementation = MaintenanceRecordResponse.class))
            ),
            @ApiResponse(responseCode = "404", description = "Propiedad no encontrada")
    })
    ResponseEntity<List<MaintenanceRecordResponse>> getMaintenanceRecordsByProperty(
            @PathVariable UUID propertyId
    );

    @Operation(
            summary = "Obtener registros por contrato",
            description = "Lista todos los registros de mantenimiento de un contrato"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista obtenida exitosamente",
                    content = @Content(schema = @Schema(implementation = MaintenanceRecordResponse.class))
            )
    })
    ResponseEntity<List<MaintenanceRecordResponse>> getMaintenanceRecordsByContract(
            @PathVariable UUID contractId
    );

    @Operation(
            summary = "Obtener registros por estado",
            description = "Filtra registros por estado (PENDIENTE, EN_PROCESO, COMPLETADO, CANCELADO)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista obtenida exitosamente",
                    content = @Content(schema = @Schema(implementation = MaintenanceRecordResponse.class))
            )
    })
    ResponseEntity<List<MaintenanceRecordResponse>> getMaintenanceRecordsByStatus(
            @RequestParam String status
    );

    @Operation(
            summary = "Obtener registros por tipo",
            description = "Filtra registros por tipo (PREVENTIVO, CORRECTIVO, EMERGENCIA)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista obtenida exitosamente",
                    content = @Content(schema = @Schema(implementation = MaintenanceRecordResponse.class))
            )
    })
    ResponseEntity<List<MaintenanceRecordResponse>> getMaintenanceRecordsByType(
            @RequestParam String type
    );

    @Operation(
            summary = "Obtener registros por categoría",
            description = "Filtra registros por categoría (PLOMERIA, ELECTRICIDAD, PINTURA, LIMPIEZA, etc)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista obtenida exitosamente",
                    content = @Content(schema = @Schema(implementation = MaintenanceRecordResponse.class))
            )
    })
    ResponseEntity<List<MaintenanceRecordResponse>> getMaintenanceRecordsByCategory(
            @RequestParam String category
    );

    @Operation(
            summary = "Obtener registros pendientes",
            description = "Lista todos los registros con estado PENDIENTE o EN_PROCESO"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista obtenida exitosamente",
                    content = @Content(schema = @Schema(implementation = MaintenanceRecordResponse.class))
            )
    })
    ResponseEntity<List<MaintenanceRecordResponse>> getPendingMaintenanceRecords();

    @Operation(
            summary = "Marcar como completado",
            description = "Marca un registro de mantenimiento como COMPLETADO"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Registro marcado como completado",
                    content = @Content(schema = @Schema(implementation = MaintenanceRecordDetailResponse.class))
            ),
            @ApiResponse(responseCode = "404", description = "Registro no encontrado")
    })
    ResponseEntity<MaintenanceRecordDetailResponse> markAsCompleted(
            @PathVariable UUID id,
            @RequestParam(required = false) String actualCost
    );

    @Operation(
            summary = "Obtener resumen de mantenimientos",
            description = "Obtiene estadísticas y resumen de mantenimientos de la organización"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Resumen obtenido exitosamente",
                    content = @Content(schema = @Schema(implementation = MaintenanceRecordSummaryResponse.class))
            )
    })
    ResponseEntity<MaintenanceRecordSummaryResponse> getMaintenanceRecordsSummary();
}
