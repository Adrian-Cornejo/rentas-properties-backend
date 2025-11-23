package com.rentas.properties.api.controller;

import com.rentas.properties.api.dto.response.PublicPropertyResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@Tag(name = "Public Properties", description = "Endpoints públicos para compartir propiedades (sin autenticación)")
public interface PublicPropertyController {

    @Operation(
            summary = "Obtener propiedad pública por ID",
            description = "Obtiene toda la información pública de una propiedad para compartir. " +
                    "NO requiere autenticación. Solo muestra propiedades activas."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Propiedad encontrada exitosamente",
                    content = @Content(schema = @Schema(implementation = PublicPropertyResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Propiedad no encontrada o inactiva"
            )
    })
    ResponseEntity<PublicPropertyResponse> getPublicProperty(@PathVariable UUID id);
}