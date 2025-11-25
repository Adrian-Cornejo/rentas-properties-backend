package com.rentas.properties.api.dto.request;

import jakarta.validation.Valid;
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

    @NotNull(message = "Se requiere al menos un inquilino")
    @Size(min = 1, message = "Debe haber al menos un inquilino en el contrato")
    @Valid
    private List<TenantAssignmentDto> tenants;

    @NotNull(message = "La fecha de inicio es obligatoria")
    private LocalDate startDate;

    @NotNull(message = "La fecha de fin es obligatoria")
    private LocalDate endDate;

    private LocalDate signedDate;

    @NotNull(message = "La renta mensual es obligatoria")
    @DecimalMin(value = "0.01", message = "La renta mensual debe ser mayor a 0")
    @Digits(integer = 10, fraction = 2, message = "Formato de renta mensual inválido")
    private BigDecimal monthlyRent;

    @NotNull(message = "El costo de agua es obligatorio")
    @DecimalMin(value = "0.00", message = "El costo del agua no puede ser negativo")
    @Digits(integer = 10, fraction = 2, message = "Formato de costo de agua inválido")
    private BigDecimal waterFee;

    @DecimalMin(value = "0.00", message = "El adelanto no puede ser negativo")
    @Digits(integer = 10, fraction = 2, message = "Formato de adelanto inválido")
    private BigDecimal advancePayment;

    @NotNull(message = "El monto del depósito es obligatorio")
    @DecimalMin(value = "0.01", message = "El depósito debe ser mayor a 0")
    @Digits(integer = 10, fraction = 2, message = "Formato de depósito inválido")
    private BigDecimal depositAmount;

    private Boolean depositPaid;

    private LocalDate depositPaymentDeadline;

    private String contractDocumentUrl;

    private String contractDocumentPublicId;

    @Size(max = 2000, message = "Las notas no deben exceder 2000 caracteres")
    private String notes;
}