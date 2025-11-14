package com.rentas.properties.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrganizationRequest {

    @NotBlank(message = "El nombre de la organización es obligatorio")
    @Size(min = 3, max = 255, message = "El nombre debe tener entre 3 y 255 caracteres")
    private String name;

    @Size(max = 1000, message = "La descripción no debe exceder 1000 caracteres")
    private String description;

    @Pattern(
            regexp = "^#[0-9A-Fa-f]{6}$",
            message = "El color primario debe ser un código hexadecimal válido (ejemplo: #3B82F6)"
    )
    private String primaryColor;

    @Pattern(
            regexp = "^#[0-9A-Fa-f]{6}$",
            message = "El color secundario debe ser un código hexadecimal válido (ejemplo: #10B981)"
    )
    private String secondaryColor;

    @Positive(message = "El máximo de usuarios debe ser un número positivo")
    private Integer maxUsers;

    @Positive(message = "El máximo de propiedades debe ser un número positivo")
    private Integer maxProperties;

    @Pattern(
            regexp = "^(free|basic|pro|enterprise)$",
            message = "El plan de suscripción debe ser: free, basic, pro o enterprise"
    )
    private String subscriptionPlan;
}