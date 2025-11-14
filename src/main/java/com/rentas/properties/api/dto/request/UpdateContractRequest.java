package com.rentas.properties.api.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
public class UpdateContractRequest {

    private LocalDate signedDate;

    @DecimalMin(value = "0.01", message = "La renta mensual debe ser mayor a 0")
    @Digits(integer = 10, fraction = 2, message = "Formato de renta mensual inválido")
    private BigDecimal monthlyRent;

    @DecimalMin(value = "0.00", message = "El costo del agua no puede ser negativo")
    @Digits(integer = 10, fraction = 2, message = "Formato de costo de agua inválido")
    private BigDecimal waterFee;

    private Boolean depositPaid;

    @Size(max = 2000, message = "La URL del documento no debe exceder 2000 caracteres")
    private String contractDocumentUrl;

    @Size(max = 255, message = "El ID público no debe exceder 255 caracteres")
    private String contractDocumentPublicId;

    @Size(max = 2000, message = "Las notas no deben exceder 2000 caracteres")
    private String notes;

    @Pattern(
            regexp = "^(ACTIVO|VENCIDO|RENOVADO|CANCELADO)$",
            message = "El estado debe ser: ACTIVO, VENCIDO, RENOVADO o CANCELADO"
    )
    private String status;
}
