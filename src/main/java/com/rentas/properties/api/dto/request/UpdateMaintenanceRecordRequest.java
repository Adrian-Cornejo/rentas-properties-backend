package com.rentas.properties.api.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateMaintenanceRecordRequest {

    @Size(min = 3, max = 255, message = "El título debe tener entre 3 y 255 caracteres")
    private String title;

    @Size(min = 10, max = 2000, message = "La descripción debe tener entre 10 y 2000 caracteres")
    private String description;

    @Pattern(
            regexp = "^(PREVENTIVO|CORRECTIVO|EMERGENCIA)$",
            message = "El tipo debe ser: PREVENTIVO, CORRECTIVO o EMERGENCIA"
    )
    private String maintenanceType;

    @Size(max = 50, message = "La categoría no debe exceder 50 caracteres")
    private String category;

    private LocalDate maintenanceDate;

    @DecimalMin(value = "0.00", message = "El costo estimado no puede ser negativo")
    @Digits(integer = 10, fraction = 2, message = "Formato de costo inválido")
    private BigDecimal estimatedCost;

    @DecimalMin(value = "0.00", message = "El costo actual no puede ser negativo")
    @Digits(integer = 10, fraction = 2, message = "Formato de costo inválido")
    private BigDecimal actualCost;

    @Pattern(
            regexp = "^(PENDIENTE|EN_PROCESO|COMPLETADO|CANCELADO)$",
            message = "El estado debe ser: PENDIENTE, EN_PROCESO, COMPLETADO o CANCELADO"
    )
    private String status;

    @Size(max = 255, message = "El responsable no debe exceder 255 caracteres")
    private String assignedTo;

    @Size(max = 2000, message = "Las notas no deben exceder 2000 caracteres")
    private String notes;
}
