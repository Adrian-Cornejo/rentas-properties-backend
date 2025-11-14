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
public class UpdateDepositStatusRequest {

    @NotBlank(message = "El estado del depósito es obligatorio")
    @Pattern(
            regexp = "^(PENDIENTE|PAGADO|RETENIDO|DEVUELTO|USADO_REPARACIONES)$",
            message = "El estado del depósito debe ser: PENDIENTE, PAGADO, RETENIDO, DEVUELTO o USADO_REPARACIONES"
    )
    private String depositStatus;

    @DecimalMin(value = "0.00", message = "El monto de devolución no puede ser negativo")
    @Digits(integer = 10, fraction = 2, message = "Formato de monto inválido")
    private BigDecimal depositReturnAmount;

    private LocalDate depositReturnDate;

    @Size(max = 2000, message = "La razón de deducción no debe exceder 2000 caracteres")
    private String depositDeductionReason;
}
