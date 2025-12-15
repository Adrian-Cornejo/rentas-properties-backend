// src/main/java/com/rentas/properties/api/controller/ISubscriptionPlanController.java
package com.rentas.properties.api.controller;

import com.rentas.properties.api.dto.response.SubscriptionPlanResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

@Tag(name = "Subscription Plans", description = "API para gestión de planes de suscripción")
public interface SubscriptionPlanController {

    @GetMapping
    @Operation(
            summary = "Obtener todos los planes activos",
            description = "Retorna la lista de todos los planes de suscripción activos ordenados por display_order"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de planes obtenida exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SubscriptionPlanResponse.class)
                    )
            )
    })
    ResponseEntity<List<SubscriptionPlanResponse>> getAllActivePlans();

    @GetMapping("/all")
    @Operation(
            summary = "Obtener todos los planes (incluyendo inactivos)",
            description = "Retorna todos los planes de suscripción, activos e inactivos. Requiere permisos de administrador."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista completa de planes obtenida",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SubscriptionPlanResponse.class)
                    )
            )
    })
    ResponseEntity<List<SubscriptionPlanResponse>> getAllPlans();

    @GetMapping("/{planId}")
    @Operation(
            summary = "Obtener plan por ID",
            description = "Retorna la información detallada de un plan específico por su UUID"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Plan encontrado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SubscriptionPlanResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Plan no encontrado"
            )
    })
    ResponseEntity<SubscriptionPlanResponse> getPlanById(
            @Parameter(description = "UUID del plan", required = true)
            @PathVariable UUID planId
    );

    @GetMapping("/code/{planCode}")
    @Operation(
            summary = "Obtener plan por código",
            description = "Retorna la información de un plan por su código (STARTER, BASICO, PROFESIONAL, EMPRESARIAL)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Plan encontrado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SubscriptionPlanResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Plan no encontrado"
            )
    })
    ResponseEntity<SubscriptionPlanResponse> getPlanByCode(
            @Parameter(description = "Código del plan (STARTER, BASICO, etc.)", required = true)
            @PathVariable String planCode
    );

    @GetMapping("/popular")
    @Operation(
            summary = "Obtener plan más popular",
            description = "Retorna el plan marcado como 'más popular' (generalmente PROFESIONAL)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Plan popular obtenido",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SubscriptionPlanResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "No hay plan marcado como popular"
            )
    })
    ResponseEntity<SubscriptionPlanResponse> getPopularPlan();

    @GetMapping("/free")
    @Operation(
            summary = "Obtener plan gratuito",
            description = "Retorna el plan gratuito (STARTER con precio = 0)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Plan gratuito obtenido",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SubscriptionPlanResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "No hay plan gratuito disponible"
            )
    })
    ResponseEntity<SubscriptionPlanResponse> getFreePlan();

    @GetMapping("/compare")
    @Operation(
            summary = "Comparar dos planes",
            description = "Compara el plan actual con un plan objetivo para evaluar upgrade/downgrade"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Comparación realizada exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SubscriptionPlanResponse.PlanComparisonResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Uno de los planes no fue encontrado"
            )
    })
    ResponseEntity<SubscriptionPlanResponse.PlanComparisonResponse> comparePlans(
            @Parameter(description = "UUID del plan actual", required = true)
            @RequestParam UUID currentPlanId,
            @Parameter(description = "UUID del plan objetivo", required = true)
            @RequestParam UUID targetPlanId
    );

    @GetMapping("/{planId}/has-feature/{featureCode}")
    @Operation(
            summary = "Verificar si plan tiene una funcionalidad",
            description = "Verifica si un plan específico incluye una funcionalidad determinada"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Verificación completada",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Boolean.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Plan no encontrado"
            )
    })
    ResponseEntity<Boolean> planHasFeature(
            @Parameter(description = "UUID del plan", required = true)
            @PathVariable UUID planId,
            @Parameter(description = "Código de la funcionalidad (NOTIFICATIONS, API_ACCESS, etc.)", required = true)
            @PathVariable String featureCode
    );

    @GetMapping("/{planId}/stats")
    @Operation(
            summary = "Obtener estadísticas de un plan",
            description = "Retorna estadísticas de uso y revenue de un plan específico"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Estadísticas obtenidas",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SubscriptionPlanResponse.PlanStatsResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Plan no encontrado"
            )
    })
    ResponseEntity<SubscriptionPlanResponse.PlanStatsResponse> getPlanStats(
            @Parameter(description = "UUID del plan", required = true)
            @PathVariable UUID planId
    );
}