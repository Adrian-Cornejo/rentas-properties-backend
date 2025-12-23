package com.rentas.properties.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request para generar reporte comparativo/ejecutivo")
public class ExecutiveReportRequest {

    @NotNull(message = "El año es requerido")
    @Min(value = 2020, message = "El año debe ser mayor o igual a 2020")
    @Max(value = 2100, message = "El año debe ser menor o igual a 2100")
    @Schema(description = "Año del reporte", example = "2024")
    private Integer year;

    @Schema(description = "Año de comparación (opcional, null = año anterior)", example = "2023")
    private Integer comparisonYear;

    @Schema(description = "Periodo específico (opcional, null = año completo)", example = "Q1", allowableValues = {"Q1", "Q2", "Q3", "Q4", "H1", "H2"})
    private String period;

    @Schema(description = "ID de ubicación específica (opcional, null = todas)", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID locationId;
}