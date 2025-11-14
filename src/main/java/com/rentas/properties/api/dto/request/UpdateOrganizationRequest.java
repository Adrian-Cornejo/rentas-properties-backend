package com.rentas.properties.api.dto.request;

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
public class UpdateOrganizationRequest {

    @Size(min = 3, max = 255, message = "El nombre debe tener entre 3 y 255 caracteres")
    private String name;

    @Size(max = 1000, message = "La descripción no debe exceder 1000 caracteres")
    private String description;

    @Size(max = 500, message = "La URL del logo no debe exceder 500 caracteres")
    private String logoUrl;

    @Size(max = 255, message = "El public ID no debe exceder 255 caracteres")
    private String logoPublicId;

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

    private Boolean codeIsReusable;

    @Positive(message = "El máximo de usuarios debe ser un número positivo")
    private Integer maxUsers;

    @Positive(message = "El máximo de propiedades debe ser un número positivo")
    private Integer maxProperties;

    @Pattern(
            regexp = "^(trial|active|suspended|cancelled)$",
            message = "El estado de suscripción debe ser: trial, active, suspended o cancelled"
    )
    private String subscriptionStatus;

    @Pattern(
            regexp = "^(free|basic|pro|enterprise)$",
            message = "El plan de suscripción debe ser: free, basic, pro o enterprise"
    )
    private String subscriptionPlan;
}