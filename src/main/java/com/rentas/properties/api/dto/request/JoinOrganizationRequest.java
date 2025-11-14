package com.rentas.properties.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JoinOrganizationRequest {

    @NotBlank(message = "El código de invitación es obligatorio")
    @Pattern(
            regexp = "^[A-Z]{3}-[A-Z0-9]{2}[A-Z][0-9]$",
            message = "El código de invitación debe tener el formato ABC-12D3"
    )
    private String invitationCode;
}