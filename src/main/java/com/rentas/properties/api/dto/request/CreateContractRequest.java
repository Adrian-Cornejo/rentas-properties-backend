package com.rentas.properties.api.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateContractRequest {

    @NotNull(message = "El ID de la propiedad es obligatorio")
    private UUID propertyId;

    @NotNull(message = "Se requiere al menos un arrendatario")
    @Size(min = 1, message = "Debe haber al menos un arrendatario en el contrato")
    private List<UUID> tenantIds;

    @NotBlank(message = "El número de contrato es obligatorio")
    @Size(max = 50, message = "El número de contrato no debe exceder 50 caracteres")
    @Pattern(
            regexp = "^CONT-[0-9]{4}-[0-9]{3}$",
            message = "El número de contrato debe tener el formato CONT-2024-001"
    )
    private String contractNumber;

    @NotNull(message = "La fecha de inicio es obligatoria")
    @FutureOrPresent(message = "La fecha de inicio debe ser hoy o en el futuro")
    private LocalDate startDate;

    @NotNull(message = "La fecha de fin es obligatoria")
    @Future(message = "La fecha de fin debe ser en el futuro")
    private LocalDate endDate;

    private LocalDate signedDate;

    @NotNull(message = "La renta mensual es obligatoria")
    @DecimalMin(value = "0.01", message = "La renta mensual debe ser mayor a 0")
    @Digits(integer = 10, fraction = 2, message = "Formato de renta mensual inválido")
    private BigDecimal monthlyRent;

    @DecimalMin(value = "0.00", message = "El costo del agua no puede ser negativo")
    @Digits(integer = 10, fraction = 2, message = "Formato de costo de agua inválido")
    private BigDecimal waterFee;

    @NotNull(message = "El monto del adelanto es obligatorio")
    @DecimalMin(value = "0.01", message = "El adelanto debe ser mayor a 0")
    @Digits(integer = 10, fraction = 2, message = "Formato de adelanto inválido")
    private BigDecimal advancePayment;

    @NotNull(message = "El monto del depósito es obligatorio")
    @DecimalMin(value = "0.01", message = "El depósito debe ser mayor a 0")
    @Digits(integer = 10, fraction = 2, message = "Formato de depósito inválido")
    private BigDecimal depositAmount;

    @Size(max = 2000, message = "Las notas no deben exceder 2000 caracteres")
    private String notes;
}
