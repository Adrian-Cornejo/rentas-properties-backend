package com.rentas.properties.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response del reporte de pagos y morosidad")
public class PaymentReportResponse {

    @Schema(description = "Fecha de inicio del periodo")
    private LocalDate startDate;

    @Schema(description = "Fecha de fin del periodo")
    private LocalDate endDate;

    @Schema(description = "Resumen de pagos")
    private PaymentSummary summary;

    @Schema(description = "Historial detallado de pagos")
    private List<PaymentDetail> paymentDetails;

    @Schema(description = "Top 5 contratos con más atrasos")
    private List<ContractDelinquency> topDelinquents;

    @Schema(description = "Tendencia de morosidad mensual")
    private List<MonthlyDelinquency> monthlyDelinquency;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentSummary {
        @Schema(description = "Total de pagos en el periodo")
        private Integer totalPayments;

        @Schema(description = "Pagos pendientes")
        private Integer pendingPayments;

        @Schema(description = "Pagos completados")
        private Integer paidPayments;

        @Schema(description = "Pagos atrasados")
        private Integer overduePayments;

        @Schema(description = "Monto total esperado")
        private BigDecimal totalExpected;

        @Schema(description = "Monto total cobrado")
        private BigDecimal totalCollected;

        @Schema(description = "Monto pendiente")
        private BigDecimal pendingAmount;

        @Schema(description = "Monto en mora")
        private BigDecimal overdueAmount;

        @Schema(description = "Tasa de morosidad (%)")
        private Double delinquencyRate;

        @Schema(description = "Promedio de días de atraso")
        private Double averageDaysOverdue;

        @Schema(description = "Eficiencia de cobro (%)")
        private Double collectionEfficiency;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentDetail {
        @Schema(description = "Número de contrato")
        private String contractNumber;

        @Schema(description = "Código de propiedad")
        private String propertyCode;

        @Schema(description = "Dirección de propiedad")
        private String propertyAddress;

        @Schema(description = "Nombre del inquilino")
        private String tenantName;

        @Schema(description = "Periodo del pago (mes)")
        private Integer periodMonth;

        @Schema(description = "Periodo del pago (año)")
        private Integer periodYear;

        @Schema(description = "Fecha de vencimiento")
        private LocalDate dueDate;

        @Schema(description = "Fecha de pago")
        private LocalDate paymentDate;

        @Schema(description = "Monto total")
        private BigDecimal amount;

        @Schema(description = "Estado del pago")
        private String status;

        @Schema(description = "Días de atraso (si aplica)")
        private Integer daysOverdue;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContractDelinquency {
        @Schema(description = "Número de contrato")
        private String contractNumber;

        @Schema(description = "Código de propiedad")
        private String propertyCode;

        @Schema(description = "Nombre del inquilino")
        private String tenantName;

        @Schema(description = "Total de pagos atrasados")
        private Integer overdueCount;

        @Schema(description = "Monto total en mora")
        private BigDecimal overdueAmount;

        @Schema(description = "Promedio de días de atraso")
        private Double averageDaysOverdue;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyDelinquency {
        @Schema(description = "Nombre del mes")
        private String month;

        @Schema(description = "Año")
        private Integer year;

        @Schema(description = "Número de pagos atrasados")
        private Integer overdueCount;

        @Schema(description = "Monto en mora")
        private BigDecimal overdueAmount;

        @Schema(description = "Tasa de morosidad (%)")
        private Double delinquencyRate;
    }
}