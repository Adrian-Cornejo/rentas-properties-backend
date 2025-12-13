package com.rentas.properties.api.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Estadísticas de notificaciones de la organización")
public class NotificationStatsResponse {

    @Schema(description = "Total de notificaciones enviadas", example = "150")
    private Integer totalSent;

    @Schema(description = "Total de notificaciones entregadas", example = "145")
    private Integer totalDelivered;

    @Schema(description = "Total de notificaciones fallidas", example = "5")
    private Integer totalFailed;

    @Schema(description = "Notificaciones enviadas este mes", example = "45")
    private Integer sentThisMonth;

    @Schema(description = "Límite mensual de notificaciones", example = "60")
    private Integer monthlyLimit;

    @Schema(description = "Créditos restantes este mes", example = "15")
    private Integer remainingCredits;

    @Schema(description = "Tasa de entrega exitosa en porcentaje", example = "96.67")
    private BigDecimal deliveryRate;

    @Schema(description = "Datos para gráfica de últimos 30 días")
    private List<ChartData> chartData;

    @Schema(description = "Últimas 10 notificaciones")
    private List<RecentNotification> recentNotifications;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChartData {
        @Schema(description = "Fecha", example = "2025-12-01")
        private LocalDate date;

        @Schema(description = "Enviadas", example = "5")
        private Integer sent;

        @Schema(description = "Entregadas", example = "5")
        private Integer delivered;

        @Schema(description = "Fallidas", example = "0")
        private Integer failed;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentNotification {
        @Schema(description = "Tipo de notificación", example = "PAYMENT_REMINDER")
        private String type;

        @Schema(description = "Canal utilizado", example = "WHATSAPP")
        private String channel;

        @Schema(description = "Estado", example = "DELIVERED")
        private String status;

        @Schema(description = "Teléfono del destinatario", example = "+52442XXXXXXX")
        private String recipientPhone;

        @Schema(description = "Fecha de envío", example = "2025-12-12T08:00:00")
        private String sentAt;
    }

    public static BigDecimal calculateDeliveryRate(int delivered, int total) {
        if (total == 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(delivered)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
    }
}