package com.rentas.properties.api.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateMaintenanceRecordRequest {

    @NotNull(message = "El ID de la propiedad es obligatorio")
    private UUID propertyId;

    private UUID contractId;

    @NotBlank(message = "El título es obligatorio")
    @Size(min = 3, max = 255, message = "El título debe tener entre 3 y 255 caracteres")
    private String title;

    @NotBlank(message = "La descripción es obligatoria")
    @Size(min = 10, max = 2000, message = "La descripción debe tener entre 10 y 2000 caracteres")
    private String description;

    @NotBlank(message = "El tipo de mantenimiento es obligatorio")
    @Pattern(
            regexp = "^(PREVENTIVO|CORRECTIVO|EMERGENCIA)$",
            message = "El tipo debe ser: PREVENTIVO, CORRECTIVO o EMERGENCIA"
    )
    private String maintenanceType;

    @Size(max = 50, message = "La categoría no debe exceder 50 caracteres")
    @Pattern(
            regexp = "^(PLOMERIA|ELECTRICIDAD|PINTURA|LIMPIEZA|CARPINTERIA|JARDINERIA|AIRE_ACONDICIONADO|OTRO)?$",
            message = "Categoría inválida"
    )
    private String category;

    @NotNull(message = "La fecha de mantenimiento es obligatoria")
    private LocalDate maintenanceDate;

    @DecimalMin(value = "0.00", message = "El costo estimado no puede ser negativo")
    @Digits(integer = 10, fraction = 2, message = "Formato de costo inválido")
    private BigDecimal estimatedCost;

    @Size(max = 255, message = "El responsable no debe exceder 255 caracteres")
    private String assignedTo;

    @Size(max = 2000, message = "Las notas no deben exceder 2000 caracteres")
    private String notes;
}
