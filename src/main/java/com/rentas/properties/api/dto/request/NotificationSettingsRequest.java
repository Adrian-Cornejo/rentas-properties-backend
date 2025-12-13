package com.rentas.properties.api.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationSettingsRequest {

    @NotNull(message = "El campo 'enabled' es obligatorio")
    private Boolean enabled;

    @Pattern(regexp = "^(SMS|WHATSAPP|BOTH)$", message = "El canal debe ser SMS, WHATSAPP o BOTH")
    private String channel;

    private Boolean adminNotifications;
}