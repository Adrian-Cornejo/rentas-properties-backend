package com.rentas.properties.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response del reporte ejecutivo/comparativo")
public class ExecutiveReportResponse {

    @Schema(description = "Año del reporte")
    private Integer year;

    @Schema(description = "Año de comparación")
    private Integer comparisonYear;

    @Schema(description = "Periodo del reporte")
    private String period;

    @Schema(description = "KPIs principales")
    private ExecutiveKPIs kpis;

    @Schema(description = "Comparativa año actual vs año anterior")
    private YearComparison yearComparison;

    @Schema(description = "ROI por propiedad")
    private List<PropertyROI> propertyROIs;

    @Schema(description = "Rentabilidad por ubicación")
    private List<LocationProfitability> locationProfitability;

    @Schema(description = "Mejores y peores propiedades")
    private PropertyPerformance propertyPerformance;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExecutiveKPIs {
        @Schema(description = "Ingresos totales")
        private BigDecimal totalRevenue;

        @Schema(description = "Gastos totales")
        private BigDecimal totalExpenses;

        @Schema(description = "Ganancia neta")
        private BigDecimal netProfit;

        @Schema(description = "Margen de ganancia (%)")
        private Double profitMargin;

        @Schema(description = "Tasa de ocupación promedio (%)")
        private Double occupancyRate;

        @Schema(description = "Tasa de morosidad (%)")
        private Double delinquencyRate;

        @Schema(description = "Eficiencia de cobro (%)")
        private Double collectionEfficiency;

        @Schema(description = "ROI promedio (%)")
        private Double averageROI;

        @Schema(description = "Número de propiedades activas")
        private Integer activeProperties;

        @Schema(description = "Número de contratos activos")
        private Integer activeContracts;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class YearComparison {
        @Schema(description = "Ingresos año actual")
        private BigDecimal currentYearRevenue;

        @Schema(description = "Ingresos año anterior")
        private BigDecimal previousYearRevenue;

        @Schema(description = "Variación en ingresos (%)")
        private Double revenueGrowth;

        @Schema(description = "Gastos año actual")
        private BigDecimal currentYearExpenses;

        @Schema(description = "Gastos año anterior")
        private BigDecimal previousYearExpenses;

        @Schema(description = "Variación en gastos (%)")
        private Double expensesGrowth;

        @Schema(description = "Ganancia año actual")
        private BigDecimal currentYearProfit;

        @Schema(description = "Ganancia año anterior")
        private BigDecimal previousYearProfit;

        @Schema(description = "Variación en ganancia (%)")
        private Double profitGrowth;

        @Schema(description = "Ocupación año actual (%)")
        private Double currentYearOccupancy;

        @Schema(description = "Ocupación año anterior (%)")
        private Double previousYearOccupancy;

        @Schema(description = "Variación en ocupación (puntos porcentuales)")
        private Double occupancyChange;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PropertyROI {
        @Schema(description = "Código de la propiedad")
        private String propertyCode;

        @Schema(description = "Dirección de la propiedad")
        private String address;

        @Schema(description = "Tipo de propiedad")
        private String propertyType;

        @Schema(description = "Ingresos totales")
        private BigDecimal totalRevenue;

        @Schema(description = "Gastos totales")
        private BigDecimal totalExpenses;

        @Schema(description = "Ganancia neta")
        private BigDecimal netProfit;

        @Schema(description = "ROI (%)")
        private Double roi;

        @Schema(description = "Tasa de ocupación (%)")
        private Double occupancyRate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LocationProfitability {
        @Schema(description = "Nombre de la ubicación")
        private String locationName;

        @Schema(description = "Número de propiedades")
        private Integer propertiesCount;

        @Schema(description = "Ingresos totales")
        private BigDecimal totalRevenue;

        @Schema(description = "Gastos totales")
        private BigDecimal totalExpenses;

        @Schema(description = "Ganancia neta")
        private BigDecimal netProfit;

        @Schema(description = "ROI promedio (%)")
        private Double averageROI;

        @Schema(description = "Tasa de ocupación promedio (%)")
        private Double averageOccupancy;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PropertyPerformance {
        @Schema(description = "Top 5 mejores propiedades por ROI")
        private List<PropertyROI> topPerformers;

        @Schema(description = "Bottom 5 peores propiedades por ROI")
        private List<PropertyROI> worstPerformers;

        @Schema(description = "Propiedades con mayor ocupación")
        private List<PropertyROI> highestOccupancy;

        @Schema(description = "Propiedades con menor ocupación")
        private List<PropertyROI> lowestOccupancy;
    }
}