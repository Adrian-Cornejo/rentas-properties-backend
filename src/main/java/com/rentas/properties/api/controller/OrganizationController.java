package com.rentas.properties.api.controller;

import com.rentas.properties.api.dto.request.CreateOrganizationRequest;
import com.rentas.properties.api.dto.request.UpdateOrganizationRequest;
import com.rentas.properties.api.dto.response.OrganizationDetailResponse;
import com.rentas.properties.api.dto.response.OrganizationResponse;
import com.rentas.properties.api.dto.response.OrganizationStatsResponse;
import io.swagger.v3.oas.annotations.Operation;
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

@Tag(name = "Organizations", description = "Endpoints para gestión de organizaciones multi-tenant")
public interface OrganizationController {

    @Operation(
            summary = "Crear nueva organización",
            description = "Crea una nueva organización. Solo usuarios ADMIN pueden crear organizaciones."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Organización creada exitosamente",
                    content = @Content(schema = @Schema(implementation = OrganizationDetailResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "403", description = "No tienes permisos para crear organizaciones"),
            @ApiResponse(responseCode = "409", description = "El código de invitación ya existe")
    })
    ResponseEntity<OrganizationDetailResponse> createOrganization(@Valid @RequestBody CreateOrganizationRequest request);

    @Operation(
            summary = "Obtener todas las organizaciones",
            description = "Lista todas las organizaciones. Solo ADMIN puede ver todas."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de organizaciones obtenida exitosamente",
                    content = @Content(schema = @Schema(implementation = OrganizationResponse.class))
            ),
            @ApiResponse(responseCode = "403", description = "No autorizado")
    })
    ResponseEntity<List<OrganizationResponse>> getAllOrganizations();

    @Operation(
            summary = "Obtener mi organización",
            description = "Obtiene la organización del usuario autenticado"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Organización encontrada",
                    content = @Content(schema = @Schema(implementation = OrganizationDetailResponse.class))
            ),
            @ApiResponse(responseCode = "404", description = "No tienes organización asignada")
    })
    ResponseEntity<OrganizationDetailResponse> getMyOrganization();

    @Operation(
            summary = "Obtener organización por ID",
            description = "Obtiene los detalles de una organización específica"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Organización encontrada",
                    content = @Content(schema = @Schema(implementation = OrganizationDetailResponse.class))
            ),
            @ApiResponse(responseCode = "404", description = "Organización no encontrada"),
            @ApiResponse(responseCode = "403", description = "No tienes acceso a esta organización")
    })
    ResponseEntity<OrganizationDetailResponse> getOrganizationById(@PathVariable UUID id);

    @Operation(
            summary = "Actualizar organización",
            description = "Actualiza la información de una organización. Solo el owner o ADMIN puede actualizar."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Organización actualizada exitosamente",
                    content = @Content(schema = @Schema(implementation = OrganizationDetailResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Organización no encontrada"),
            @ApiResponse(responseCode = "403", description = "No tienes permisos para actualizar esta organización")
    })
    ResponseEntity<OrganizationDetailResponse> updateOrganization(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateOrganizationRequest request
    );

    @Operation(
            summary = "Eliminar organización",
            description = "Desactiva una organización. Solo ADMIN puede eliminar."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Organización eliminada exitosamente"),
            @ApiResponse(responseCode = "404", description = "Organización no encontrada"),
            @ApiResponse(responseCode = "403", description = "No tienes permisos para eliminar organizaciones")
    })
    ResponseEntity<Void> deleteOrganization(@PathVariable UUID id);

    @Operation(
            summary = "Regenerar código de invitación",
            description = "Genera un nuevo código de invitación para la organización"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Código regenerado exitosamente",
                    content = @Content(schema = @Schema(implementation = OrganizationDetailResponse.class))
            ),
            @ApiResponse(responseCode = "404", description = "Organización no encontrada"),
            @ApiResponse(responseCode = "403", description = "No tienes permisos")
    })
    ResponseEntity<OrganizationDetailResponse> regenerateInvitationCode(@PathVariable UUID id);

    @Operation(
            summary = "Validar código de invitación",
            description = "Valida si un código de invitación es válido y está activo"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Código válido",
                    content = @Content(schema = @Schema(implementation = OrganizationResponse.class))
            ),
            @ApiResponse(responseCode = "404", description = "Código inválido o expirado")
    })
    ResponseEntity<OrganizationResponse> validateInvitationCode(@RequestParam String code);

    @Operation(
            summary = "Obtener estadísticas de la organización",
            description = "Obtiene estadísticas detalladas de usuarios, propiedades y límites"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Estadísticas obtenidas exitosamente",
                    content = @Content(schema = @Schema(implementation = OrganizationStatsResponse.class))
            ),
            @ApiResponse(responseCode = "404", description = "Organización no encontrada"),
            @ApiResponse(responseCode = "403", description = "No tienes acceso a esta organización")
    })
    ResponseEntity<OrganizationStatsResponse> getOrganizationStats(@PathVariable UUID id);

    @Operation(
            summary = "Obtener estadísticas de mi organización",
            description = "Obtiene estadísticas de la organización del usuario autenticado"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Estadísticas obtenidas exitosamente",
                    content = @Content(schema = @Schema(implementation = OrganizationStatsResponse.class))
            ),
            @ApiResponse(responseCode = "404", description = "No tienes organización asignada")
    })
    ResponseEntity<OrganizationStatsResponse> getMyOrganizationStats();

    @Operation(
            summary = "Obtener organizaciones activas",
            description = "Lista todas las organizaciones activas"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista obtenida exitosamente",
                    content = @Content(schema = @Schema(implementation = OrganizationResponse.class))
            )
    })
    ResponseEntity<List<OrganizationResponse>> getActiveOrganizations();
}