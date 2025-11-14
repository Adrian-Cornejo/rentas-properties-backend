package com.rentas.properties.api.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddLateFeeRequest {

    @Builder.Default
    private boolean autoCalculate = true;

    @DecimalMin(value = "0.01", message = "El monto del recargo debe ser mayor a 0")
    @Digits(integer = 10, fraction = 2, message = "Formato de monto inválido")
    private BigDecimal lateFeeAmount;

    @Size(max = 500, message = "La razón no debe exceder 500 caracteres")
    private String reason;
}
