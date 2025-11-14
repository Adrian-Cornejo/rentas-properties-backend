package com.rentas.properties.api.controller;

import com.rentas.properties.api.dto.request.CreateLocationRequest;
import com.rentas.properties.api.dto.request.UpdateLocationRequest;
import com.rentas.properties.api.dto.response.LocationDetailResponse;
import com.rentas.properties.api.dto.response.LocationResponse;
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

@Tag(name = "Locations", description = "Endpoints para gestión de ubicaciones/colonias")
public interface LocationController {

    @Operation(
            summary = "Crear nueva ubicación",
            description = "Crea una nueva ubicación/colonia para la organización del usuario"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Ubicación creada exitosamente",
                    content = @Content(schema = @Schema(implementation = LocationDetailResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "409", description = "Ya existe una ubicación con ese nombre en la organización"),
            @ApiResponse(responseCode = "403", description = "No tienes permisos")
    })
    ResponseEntity<LocationDetailResponse> createLocation(@Valid @RequestBody CreateLocationRequest request);

    @Operation(
            summary = "Obtener todas las ubicaciones",
            description = "Lista todas las ubicaciones de la organización del usuario"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista obtenida exitosamente",
                    content = @Content(schema = @Schema(implementation = LocationResponse.class))
            )
    })
    ResponseEntity<List<LocationResponse>> getAllLocations(
            @Parameter(description = "Incluir ubicaciones inactivas")
            @RequestParam(required = false, defaultValue = "false") boolean includeInactive
    );

    @Operation(
            summary = "Obtener ubicación por ID",
            description = "Obtiene los detalles de una ubicación específica"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Ubicación encontrada",
                    content = @Content(schema = @Schema(implementation = LocationDetailResponse.class))
            ),
            @ApiResponse(responseCode = "404", description = "Ubicación no encontrada"),
            @ApiResponse(responseCode = "403", description = "No tienes acceso a esta ubicación")
    })
    ResponseEntity<LocationDetailResponse> getLocationById(@PathVariable UUID id);

    @Operation(
            summary = "Actualizar ubicación",
            description = "Actualiza la información de una ubicación"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Ubicación actualizada exitosamente",
                    content = @Content(schema = @Schema(implementation = LocationDetailResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Ubicación no encontrada"),
            @ApiResponse(responseCode = "403", description = "No tienes permisos")
    })
    ResponseEntity<LocationDetailResponse> updateLocation(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateLocationRequest request
    );

    @Operation(
            summary = "Eliminar ubicación",
            description = "Desactiva una ubicación (soft delete)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ubicación eliminada exitosamente"),
            @ApiResponse(responseCode = "404", description = "Ubicación no encontrada"),
            @ApiResponse(responseCode = "400", description = "No se puede eliminar porque tiene propiedades asociadas"),
            @ApiResponse(responseCode = "403", description = "No tienes permisos")
    })
    ResponseEntity<Void> deleteLocation(@PathVariable UUID id);

    @Operation(
            summary = "Obtener ubicaciones por ciudad",
            description = "Lista las ubicaciones de una ciudad específica"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista obtenida exitosamente",
                    content = @Content(schema = @Schema(implementation = LocationResponse.class))
            )
    })
    ResponseEntity<List<LocationResponse>> getLocationsByCity(@RequestParam String city);

    @Operation(
            summary = "Obtener ubicaciones activas de la organización",
            description = "Lista solo las ubicaciones activas de la organización"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista obtenida exitosamente",
                    content = @Content(schema = @Schema(implementation = LocationResponse.class))
            )
    })
    ResponseEntity<List<LocationResponse>> getActiveLocations();
}