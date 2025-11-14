package com.rentas.properties.api.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTenantRequest {

    @NotBlank(message = "El nombre completo es obligatorio")
    @Size(min = 3, max = 255, message = "El nombre debe tener entre 3 y 255 caracteres")
    private String fullName;

    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(
            regexp = "^[0-9]{10}$",
            message = "El teléfono debe tener 10 dígitos"
    )
    private String phone;

    @Email(message = "El email debe ser válido")
    @Size(max = 255, message = "El email no debe exceder 255 caracteres")
    private String email;

    @Size(max = 50, message = "El número de INE no debe exceder 50 caracteres")
    private String ineNumber;

    @Size(max = 500, message = "La URL de la imagen no debe exceder 500 caracteres")
    private String ineImageUrl;

    @Size(max = 255, message = "El public ID no debe exceder 255 caracteres")
    private String inePublicId;

    @Positive(message = "El número de ocupantes debe ser un número positivo")
    @Max(value = 20, message = "El número de ocupantes no puede exceder 20")
    private Integer numberOfOccupants;

    @Size(max = 2000, message = "Las notas no deben exceder 2000 caracteres")
    private String notes;
}