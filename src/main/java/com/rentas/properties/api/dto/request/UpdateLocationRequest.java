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
public class UpdateLocationRequest {

    @Size(min = 3, max = 255, message = "El nombre debe tener entre 3 y 255 caracteres")
    private String name;

    @Size(max = 1000, message = "La dirección no debe exceder 1000 caracteres")
    private String address;

    @Size(max = 100, message = "La ciudad no debe exceder 100 caracteres")
    private String city;

    @Size(max = 100, message = "El estado no debe exceder 100 caracteres")
    private String state;

    @Pattern(
            regexp = "^[0-9]{5}$",
            message = "El código postal debe tener 5 dígitos"
    )
    private String postalCode;

    @Size(max = 1000, message = "La descripción no debe exceder 1000 caracteres")
    private String description;
}