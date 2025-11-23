package com.rentas.properties.api.controller;

import com.rentas.properties.api.dto.request.CreateTenantRequest;
import com.rentas.properties.api.dto.request.UpdateTenantRequest;
import com.rentas.properties.api.dto.response.TenantDetailResponse;
import com.rentas.properties.api.dto.response.TenantResponse;
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

@Tag(name = "Tenants", description = "Endpoints para gestión de arrendatarios/inquilinos")
public interface TenantController {

    @Operation(
            summary = "Crear nuevo arrendatario",
            description = "Crea un nuevo arrendatario para la organización del usuario"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Arrendatario creado exitosamente",
                    content = @Content(schema = @Schema(implementation = TenantDetailResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "409", description = "Ya existe un arrendatario con ese teléfono en la organización")
    })
    ResponseEntity<TenantDetailResponse> createTenant(@Valid @RequestBody CreateTenantRequest request);

    @Operation(
            summary = "Obtener todos los arrendatarios",
            description = "Lista todos los arrendatarios de la organización del usuario"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista obtenida exitosamente",
                    content = @Content(schema = @Schema(implementation = TenantResponse.class))
            )
    })
    ResponseEntity<List<TenantResponse>> getAllTenants(
            @Parameter(description = "Incluir arrendatarios inactivos")
            @RequestParam(required = false, defaultValue = "false") boolean includeInactive
    );

    @Operation(
            summary = "Obtener arrendatario por ID",
            description = "Obtiene los detalles de un arrendatario específico"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Arrendatario encontrado",
                    content = @Content(schema = @Schema(implementation = TenantDetailResponse.class))
            ),
            @ApiResponse(responseCode = "404", description = "Arrendatario no encontrado"),
            @ApiResponse(responseCode = "403", description = "No tienes acceso a este arrendatario")
    })
    ResponseEntity<TenantDetailResponse> getTenantById(@PathVariable UUID id);

    @Operation(
            summary = "Actualizar arrendatario",
            description = "Actualiza la información de un arrendatario"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Arrendatario actualizado exitosamente",
                    content = @Content(schema = @Schema(implementation = TenantDetailResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Arrendatario no encontrado"),
            @ApiResponse(responseCode = "403", description = "No tienes permisos")
    })
    ResponseEntity<TenantDetailResponse> updateTenant(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTenantRequest request
    );

    @Operation(
            summary = "Eliminar arrendatario",
            description = "Desactiva un arrendatario (soft delete)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Arrendatario eliminado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Arrendatario no encontrado"),
            @ApiResponse(responseCode = "400", description = "No se puede eliminar porque tiene contratos activos"),
            @ApiResponse(responseCode = "403", description = "No tienes permisos")
    })
    ResponseEntity<Void> deleteTenant(@PathVariable UUID id);

    @Operation(
            summary = "Buscar arrendatarios por nombre",
            description = "Busca arrendatarios por nombre (búsqueda parcial)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista obtenida exitosamente",
                    content = @Content(schema = @Schema(implementation = TenantResponse.class))
            )
    })
    ResponseEntity<List<TenantResponse>> searchTenantsByName(@RequestParam String name);

    @Operation(
            summary = "Buscar arrendatario por teléfono",
            description = "Busca un arrendatario específico por su número de teléfono"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Arrendatario encontrado",
                    content = @Content(schema = @Schema(implementation = TenantDetailResponse.class))
            ),
            @ApiResponse(responseCode = "404", description = "Arrendatario no encontrado")
    })
    ResponseEntity<TenantDetailResponse> getTenantByPhone(@RequestParam String phone);

    @Operation(
            summary = "Obtener arrendatarios activos",
            description = "Lista solo los arrendatarios activos de la organización"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista obtenida exitosamente",
                    content = @Content(schema = @Schema(implementation = TenantResponse.class))
            )
    })
    ResponseEntity<List<TenantResponse>> getActiveTenants();

    @Operation(
            summary = "Eliminar imagen de INE",
            description = "Elimina la imagen de INE del arrendatario"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Imagen eliminada exitosamente"),
            @ApiResponse(responseCode = "404", description = "Arrendatario no encontrado"),
            @ApiResponse(responseCode = "403", description = "No tienes acceso a este arrendatario")
    })
    ResponseEntity<Void> deleteTenantIneImage(@PathVariable UUID id);
}