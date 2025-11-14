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
public class UpdateUserRequest {

    @Size(min = 3, max = 255, message = "El nombre debe tener entre 3 y 255 caracteres")
    private String fullName;

    @Pattern(
            regexp = "^[0-9]{10}$",
            message = "El teléfono debe tener 10 dígitos"
    )
    private String phone;

    @Pattern(
            regexp = "^(USER|ADMIN)$",
            message = "El rol debe ser USER o ADMIN"
    )
    private String role;

    @Pattern(
            regexp = "^(pending|active|suspended)$",
            message = "El estado debe ser: pending, active o suspended"
    )
    private String accountStatus;
}