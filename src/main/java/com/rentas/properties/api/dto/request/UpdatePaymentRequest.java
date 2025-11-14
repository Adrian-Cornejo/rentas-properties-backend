package com.rentas.properties.api.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePaymentRequest {

    @Pattern(
            regexp = "^(EFECTIVO|TRANSFERENCIA|TARJETA|CHEQUE|DEPOSITO)$",
            message = "El método de pago debe ser: EFECTIVO, TRANSFERENCIA, TARJETA, CHEQUE o DEPOSITO"
    )
    private String paymentMethod;

    @Size(max = 100, message = "El número de referencia no debe exceder 100 caracteres")
    private String referenceNumber;

    @Size(max = 1000, message = "Las notas no deben exceder 1000 caracteres")
    private String notes;
}
