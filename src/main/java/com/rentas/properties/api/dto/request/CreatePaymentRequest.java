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
public class CreatePaymentRequest {

    @NotNull(message = "El ID del contrato es obligatorio")
    private UUID contractId;

    @NotBlank(message = "El tipo de pago es obligatorio")
    @Pattern(
            regexp = "^(RENTA|AGUA|DEPOSITO|ADELANTO)$",
            message = "El tipo de pago debe ser: RENTA, AGUA, DEPOSITO o ADELANTO"
    )
    private String paymentType;

    @NotNull(message = "La fecha de pago es obligatoria")
    private LocalDate paymentDate;

    @NotNull(message = "La fecha de vencimiento es obligatoria")
    private LocalDate dueDate;

    @NotNull(message = "El mes del periodo es obligatorio")
    @Min(value = 1, message = "El mes debe estar entre 1 y 12")
    @Max(value = 12, message = "El mes debe estar entre 1 y 12")
    private Integer periodMonth;

    @NotNull(message = "El año del periodo es obligatorio")
    @Min(value = 2020, message = "El año debe ser mayor o igual a 2020")
    private Integer periodYear;

    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a 0")
    @Digits(integer = 10, fraction = 2, message = "Formato de monto inválido")
    private BigDecimal amount;

    @DecimalMin(value = "0.00", message = "El recargo no puede ser negativo")
    @Digits(integer = 10, fraction = 2, message = "Formato de recargo inválido")
    private BigDecimal lateFee;
}
