package com.rentas.properties.api.controller;

import com.rentas.properties.api.dto.response.DashboardResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Dashboard", description = "Endpoint para obtener toda la información del dashboard")
public interface DashboardController {

    @Operation(
            summary = "Obtener datos del dashboard",
            description = "Retorna toda la información necesaria para el dashboard en una sola llamada: estadísticas generales, alertas, y datos para gráficas"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Datos obtenidos exitosamente",
                    content = @Content(schema = @Schema(implementation = DashboardResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "No tienes permisos")
    })
    ResponseEntity<DashboardResponse> getDashboardData();
}