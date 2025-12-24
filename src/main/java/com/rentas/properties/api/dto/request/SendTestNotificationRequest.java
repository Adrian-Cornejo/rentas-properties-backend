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
public class SendTestNotificationRequest {

    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(regexp = "^\\+52[0-9]{11}$", message = "El teléfono debe estar en formato +52XXXXXXXXXX")
    private String phoneNumber;

    @NotBlank(message = "El canal es obligatorio")
    @Pattern(regexp = "^(SMS|WHATSAPP)$", message = "El canal debe ser SMS o WHATSAPP")
    private String channel;
}