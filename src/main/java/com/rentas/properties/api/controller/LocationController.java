package com.rentas.properties.api.controller;

import com.rentas.properties.api.dto.request.CreateLocationRequest;
import com.rentas.properties.api.dto.request.UpdateLocationRequest;
import com.rentas.properties.api.dto.response.LocationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Locations", description = "Endpoints para gestión de ubicaciones/colonias")
@SecurityRequirement(name = "bearerAuth")
public interface LocationController {

    @Operation(
            summary = "Listar todas las ubicaciones",
            description = "Obtiene todas las ubicaciones activas del sistema"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de ubicaciones obtenida exitosamente",
                    content = @Content(schema = @Schema(implementation = LocationResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    ResponseEntity<List<LocationResponse>> findAll(
            @Parameter(description = "Incluir ubicaciones inactivas")
            @RequestParam(required = false, defaultValue = "false") boolean includeInactive
    );

    @Operation(
            summary = "Obtener ubicación por ID",
            description = "Obtiene una ubicación específica por su ID"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Ubicación encontrada",
                    content = @Content(schema = @Schema(implementation = LocationResponse.class))
            ),
            @ApiResponse(responseCode = "404", description = "Ubicación no encontrada"),
            @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    ResponseEntity<LocationResponse> findById(
            @Parameter(description = "ID de la ubicación", required = true)
            @PathVariable UUID id
    );

    @Operation(
            summary = "Crear nueva ubicación",
            description = "Crea una nueva ubicación en el sistema"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Ubicación creada exitosamente",
                    content = @Content(schema = @Schema(implementation = LocationResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "409", description = "La ubicación ya existe"),
            @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    ResponseEntity<LocationResponse> create(
            @Valid @RequestBody CreateLocationRequest request
    );

    @Operation(
            summary = "Actualizar ubicación",
            description = "Actualiza los datos de una ubicación existente"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Ubicación actualizada exitosamente",
                    content = @Content(schema = @Schema(implementation = LocationResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Ubicación no encontrada"),
            @ApiResponse(responseCode = "409", description = "Nombre duplicado"),
            @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    ResponseEntity<LocationResponse> update(
            @Parameter(description = "ID de la ubicación", required = true)
            @PathVariable UUID id,
            @Valid @RequestBody UpdateLocationRequest request
    );

    @Operation(
            summary = "Eliminar ubicación",
            description = "Desactiva una ubicación del sistema (soft delete)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Ubicación eliminada exitosamente"),
            @ApiResponse(responseCode = "404", description = "Ubicación no encontrada"),
            @ApiResponse(responseCode = "400", description = "No se puede eliminar - tiene propiedades asociadas"),
            @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    ResponseEntity<Void> delete(
            @Parameter(description = "ID de la ubicación", required = true)
            @PathVariable UUID id
    );

    @Operation(
            summary = "Buscar ubicaciones por ciudad",
            description = "Busca ubicaciones en una ciudad específica"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de ubicaciones encontradas",
                    content = @Content(schema = @Schema(implementation = LocationResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    ResponseEntity<List<LocationResponse>> findByCity(
            @Parameter(description = "Nombre de la ciudad", required = true)
            @PathVariable String city
    );
}