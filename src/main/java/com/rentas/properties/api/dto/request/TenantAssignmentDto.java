package com.rentas.properties.api.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantAssignmentDto {

    @NotNull(message = "El ID del inquilino es obligatorio")
    private UUID tenantId;

    @NotNull(message = "Debe especificar si es el inquilino principal")
    private Boolean isPrimary;

    private String relationship;
}