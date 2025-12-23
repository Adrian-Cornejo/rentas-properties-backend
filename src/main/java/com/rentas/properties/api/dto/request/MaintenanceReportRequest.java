package com.rentas.properties.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request para generar reporte de mantenimientos")
public class MaintenanceReportRequest {

    @NotNull(message = "La fecha de inicio es requerida")
    @Schema(description = "Fecha de inicio del reporte", example = "2024-01-01")
    private LocalDate startDate;

    @NotNull(message = "La fecha de fin es requerida")
    @Schema(description = "Fecha de fin del reporte", example = "2024-12-31")
    private LocalDate endDate;

    @Schema(description = "Tipo de mantenimiento (opcional, null = todos)", example = "PREVENTIVO", allowableValues = {"PREVENTIVO", "CORRECTIVO", "EMERGENCIA"})
    private String maintenanceType;

    @Schema(description = "Estado del mantenimiento (opcional, null = todos)", example = "COMPLETADO", allowableValues = {"PENDIENTE", "EN_PROCESO", "COMPLETADO", "CANCELADO"})
    private String status;

    @Schema(description = "ID de propiedad específica (opcional, null = todas)", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID propertyId;

    @Schema(description = "Categoría del mantenimiento (opcional, null = todas)", example = "PLOMERIA")
    private String category;
}