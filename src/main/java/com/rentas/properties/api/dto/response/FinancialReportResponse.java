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
@Schema(description = "Response del reporte financiero")
public class FinancialReportResponse {

    @Schema(description = "Fecha de inicio del periodo")
    private LocalDate startDate;

    @Schema(description = "Fecha de fin del periodo")
    private LocalDate endDate;

    @Schema(description = "Resumen financiero")
    private FinancialSummary summary;

    @Schema(description = "Ingresos mensuales")
    private List<MonthlyIncome> monthlyIncomes;

    @Schema(description = "Ingresos por propiedad")
    private List<PropertyIncome> propertyIncomes;

    @Schema(description = "Comparativa con periodo anterior")
    private PeriodComparison comparison;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FinancialSummary {
        @Schema(description = "Ingresos totales del periodo")
        private BigDecimal totalIncome;

        @Schema(description = "Ingresos por rentas")
        private BigDecimal rentIncome;

        @Schema(description = "Ingresos por agua")
        private BigDecimal waterIncome;

        @Schema(description = "Gastos de mantenimiento")
        private BigDecimal maintenanceExpenses;

        @Schema(description = "Ganancia neta (ingresos - gastos)")
        private BigDecimal netProfit;

        @Schema(description = "Margen de ganancia (%)")
        private Double profitMargin;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyIncome {
        @Schema(description = "Nombre del mes")
        private String month;

        @Schema(description = "Año")
        private Integer year;

        @Schema(description = "Ingresos del mes")
        private BigDecimal income;

        @Schema(description = "Gastos del mes")
        private BigDecimal expenses;

        @Schema(description = "Ganancia neta del mes")
        private BigDecimal netProfit;

        @Schema(description = "Número de pagos recibidos")
        private Integer paymentsCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PropertyIncome {
        @Schema(description = "Código de la propiedad")
        private String propertyCode;

        @Schema(description = "Dirección de la propiedad")
        private String address;

        @Schema(description = "Tipo de propiedad")
        private String propertyType;

        @Schema(description = "Ingresos totales de la propiedad")
        private BigDecimal totalIncome;

        @Schema(description = "Gastos de mantenimiento de la propiedad")
        private BigDecimal maintenanceExpenses;

        @Schema(description = "Ganancia neta de la propiedad")
        private BigDecimal netProfit;

        @Schema(description = "ROI (%)")
        private Double roi;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PeriodComparison {
        @Schema(description = "Ingresos periodo actual")
        private BigDecimal currentIncome;

        @Schema(description = "Ingresos periodo anterior")
        private BigDecimal previousIncome;

        @Schema(description = "Variación absoluta")
        private BigDecimal absoluteChange;

        @Schema(description = "Variación porcentual")
        private Double percentageChange;

        @Schema(description = "Tendencia (UP, DOWN, STABLE)")
        private String trend;
    }
}