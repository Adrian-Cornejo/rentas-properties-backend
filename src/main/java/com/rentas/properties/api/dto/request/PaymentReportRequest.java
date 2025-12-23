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
@Schema(description = "Request para generar reporte de pagos y morosidad")
public class PaymentReportRequest {

    @NotNull(message = "La fecha de inicio es requerida")
    @Schema(description = "Fecha de inicio del reporte", example = "2024-01-01")
    private LocalDate startDate;

    @NotNull(message = "La fecha de fin es requerida")
    @Schema(description = "Fecha de fin del reporte", example = "2024-12-31")
    private LocalDate endDate;

    @Schema(description = "Estado de pago (opcional, null = todos)", example = "ATRASADO", allowableValues = {"PENDIENTE", "PAGADO", "ATRASADO", "PARCIAL"})
    private String paymentStatus;

    @Schema(description = "ID de propiedad específica (opcional, null = todas)", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID propertyId;

    @Schema(description = "ID de contrato específico (opcional, null = todos)", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID contractId;
}