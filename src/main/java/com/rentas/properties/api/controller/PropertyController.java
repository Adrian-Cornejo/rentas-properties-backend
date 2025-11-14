package com.rentas.properties.api.controller;

import com.rentas.properties.api.dto.request.CreatePropertyRequest;
import com.rentas.properties.api.dto.request.UpdatePropertyRequest;
import com.rentas.properties.api.dto.response.PropertyDetailResponse;
import com.rentas.properties.api.dto.response.PropertyResponse;
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

@Tag(name = "Properties", description = "Endpoints para gestión de propiedades en renta")
public interface PropertyController {

    @Operation(
            summary = "Crear nueva propiedad",
            description = "Crea una nueva propiedad para la organización del usuario. Valida límites según el plan."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Propiedad creada exitosamente",
                    content = @Content(schema = @Schema(implementation = PropertyDetailResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "409", description = "Ya existe una propiedad con ese código o se alcanzó el límite de propiedades"),
            @ApiResponse(responseCode = "403", description = "No tienes permisos")
    })
    ResponseEntity<PropertyDetailResponse> createProperty(@Valid @RequestBody CreatePropertyRequest request);

    @Operation(
            summary = "Obtener todas las propiedades",
            description = "Lista todas las propiedades de la organización del usuario"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista obtenida exitosamente",
                    content = @Content(schema = @Schema(implementation = PropertyResponse.class))
            )
    })
    ResponseEntity<List<PropertyResponse>> getAllProperties(
            @Parameter(description = "Incluir propiedades inactivas")
            @RequestParam(required = false, defaultValue = "false") boolean includeInactive
    );

    @Operation(
            summary = "Obtener propiedad por ID",
            description = "Obtiene los detalles de una propiedad específica"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Propiedad encontrada",
                    content = @Content(schema = @Schema(implementation = PropertyDetailResponse.class))
            ),
            @ApiResponse(responseCode = "404", description = "Propiedad no encontrada"),
            @ApiResponse(responseCode = "403", description = "No tienes acceso a esta propiedad")
    })
    ResponseEntity<PropertyDetailResponse> getPropertyById(@PathVariable UUID id);

    @Operation(
            summary = "Obtener propiedad por código",
            description = "Busca una propiedad por su código único"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Propiedad encontrada",
                    content = @Content(schema = @Schema(implementation = PropertyDetailResponse.class))
            ),
            @ApiResponse(responseCode = "404", description = "Propiedad no encontrada")
    })
    ResponseEntity<PropertyDetailResponse> getPropertyByCode(@RequestParam String code);

    @Operation(
            summary = "Actualizar propiedad",
            description = "Actualiza la información de una propiedad"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Propiedad actualizada exitosamente",
                    content = @Content(schema = @Schema(implementation = PropertyDetailResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Propiedad no encontrada"),
            @ApiResponse(responseCode = "403", description = "No tienes permisos")
    })
    ResponseEntity<PropertyDetailResponse> updateProperty(
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePropertyRequest request
    );

    @Operation(
            summary = "Eliminar propiedad",
            description = "Desactiva una propiedad (soft delete). Decrementa el contador de la organización."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Propiedad eliminada exitosamente"),
            @ApiResponse(responseCode = "404", description = "Propiedad no encontrada"),
            @ApiResponse(responseCode = "400", description = "No se puede eliminar porque tiene contratos activos"),
            @ApiResponse(responseCode = "403", description = "No tienes permisos")
    })
    ResponseEntity<Void> deleteProperty(@PathVariable UUID id);

    @Operation(
            summary = "Obtener propiedades por estado",
            description = "Lista propiedades según su estado (DISPONIBLE, RENTADA, MANTENIMIENTO)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista obtenida exitosamente",
                    content = @Content(schema = @Schema(implementation = PropertyResponse.class))
            )
    })
    ResponseEntity<List<PropertyResponse>> getPropertiesByStatus(@RequestParam String status);

    @Operation(
            summary = "Obtener propiedades por ubicación",
            description = "Lista todas las propiedades de una ubicación específica"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista obtenida exitosamente",
                    content = @Content(schema = @Schema(implementation = PropertyResponse.class))
            )
    })
    ResponseEntity<List<PropertyResponse>> getPropertiesByLocation(@PathVariable UUID locationId);

    @Operation(
            summary = "Obtener propiedades por tipo",
            description = "Lista propiedades por tipo (CASA, DEPARTAMENTO, LOCAL_COMERCIAL)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista obtenida exitosamente",
                    content = @Content(schema = @Schema(implementation = PropertyResponse.class))
            )
    })
    ResponseEntity<List<PropertyResponse>> getPropertiesByType(@RequestParam String type);

    @Operation(
            summary = "Obtener propiedades disponibles",
            description = "Lista solo las propiedades disponibles para rentar"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista obtenida exitosamente",
                    content = @Content(schema = @Schema(implementation = PropertyResponse.class))
            )
    })
    ResponseEntity<List<PropertyResponse>> getAvailableProperties();

    @Operation(
            summary = "Obtener propiedades rentadas",
            description = "Lista solo las propiedades actualmente rentadas"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista obtenida exitosamente",
                    content = @Content(schema = @Schema(implementation = PropertyResponse.class))
            )
    })
    ResponseEntity<List<PropertyResponse>> getRentedProperties();
}