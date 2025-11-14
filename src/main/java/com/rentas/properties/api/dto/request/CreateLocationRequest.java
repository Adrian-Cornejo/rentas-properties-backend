package com.rentas.properties.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateLocationRequest {

    @NotBlank(message = "El nombre de la ubicación es obligatorio")
    @Size(max = 255, message = "El nombre no debe exceder 255 caracteres")
    private String name;

    @Size(max = 1000, message = "La dirección no debe exceder 1000 caracteres")
    private String address;

    @Size(max = 100, message = "La ciudad no debe exceder 100 caracteres")
    private String city;

    @Size(max = 100, message = "El estado no debe exceder 100 caracteres")
    private String state;

    @Size(max = 10, message = "El código postal no debe exceder 10 caracteres")
    private String postalCode;

    @Size(max = 2000, message = "La descripción no debe exceder 2000 caracteres")
    private String description;
}