package com.rentas.properties.api.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Configuración de notificaciones de la organización")
public class NotificationSettingsResponse {

    @Schema(description = "Si las notificaciones están habilitadas", example = "true")
    private Boolean enabled;

    @Schema(description = "Canal de notificaciones configurado", example = "WHATSAPP")
    private String channel;

    @Schema(description = "Si el admin recibe notificaciones consolidadas", example = "true")
    private Boolean adminNotifications;

    @Schema(description = "Notificaciones enviadas este mes", example = "45")
    private Integer sentThisMonth;

    @Schema(description = "Límite mensual", example = "60")
    private Integer monthlyLimit;

    @Schema(description = "Créditos restantes", example = "15")
    private Integer remainingCredits;

    @Schema(description = "Plan de suscripción", example = "INTERMEDIO")
    private String subscriptionPlan;
}