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
@Schema(description = "Response del reporte de mantenimientos")
public class MaintenanceReportResponse {

    @Schema(description = "Fecha de inicio del periodo")
    private LocalDate startDate;

    @Schema(description = "Fecha de fin del periodo")
    private LocalDate endDate;

    @Schema(description = "Resumen de mantenimientos")
    private MaintenanceSummary summary;

    @Schema(description = "Desglose por tipo de mantenimiento")
    private TypeBreakdown typeBreakdown;

    @Schema(description = "Desglose por categoría")
    private List<CategoryBreakdown> categoryBreakdown;

    @Schema(description = "Mantenimientos por propiedad")
    private List<PropertyMaintenance> propertyMaintenances;

    @Schema(description = "Comparativa costos estimados vs reales")
    private CostComparison costComparison;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MaintenanceSummary {
        @Schema(description = "Total de mantenimientos")
        private Integer totalMaintenances;

        @Schema(description = "Mantenimientos pendientes")
        private Integer pending;

        @Schema(description = "Mantenimientos en proceso")
        private Integer inProgress;

        @Schema(description = "Mantenimientos completados")
        private Integer completed;

        @Schema(description = "Mantenimientos cancelados")
        private Integer canceled;

        @Schema(description = "Costo total estimado")
        private BigDecimal totalEstimatedCost;

        @Schema(description = "Costo total real")
        private BigDecimal totalActualCost;

        @Schema(description = "Variación de costos (%)")
        private Double costVariance;

        @Schema(description = "Frecuencia promedio de mantenimientos (días entre mantenimientos)")
        private Double averageFrequency;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TypeBreakdown {
        @Schema(description = "Mantenimientos preventivos")
        private Integer preventive;

        @Schema(description = "Mantenimientos correctivos")
        private Integer corrective;

        @Schema(description = "Mantenimientos de emergencia")
        private Integer emergency;

        @Schema(description = "Costo mantenimientos preventivos")
        private BigDecimal preventiveCost;

        @Schema(description = "Costo mantenimientos correctivos")
        private BigDecimal correctiveCost;

        @Schema(description = "Costo mantenimientos de emergencia")
        private BigDecimal emergencyCost;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryBreakdown {
        @Schema(description = "Categoría del mantenimiento")
        private String category;

        @Schema(description = "Cantidad de mantenimientos")
        private Integer count;

        @Schema(description = "Costo total de la categoría")
        private BigDecimal totalCost;

        @Schema(description = "Porcentaje del total")
        private Double percentage;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PropertyMaintenance {
        @Schema(description = "Código de la propiedad")
        private String propertyCode;

        @Schema(description = "Dirección de la propiedad")
        private String address;

        @Schema(description = "Cantidad de mantenimientos")
        private Integer maintenanceCount;

        @Schema(description = "Costo total estimado")
        private BigDecimal estimatedCost;

        @Schema(description = "Costo total real")
        private BigDecimal actualCost;

        @Schema(description = "Tipo de mantenimiento más frecuente")
        private String mostFrequentType;

        @Schema(description = "Categoría más frecuente")
        private String mostFrequentCategory;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CostComparison {
        @Schema(description = "Total estimado")
        private BigDecimal totalEstimated;

        @Schema(description = "Total real")
        private BigDecimal totalActual;

        @Schema(description = "Diferencia absoluta")
        private BigDecimal absoluteDifference;

        @Schema(description = "Diferencia porcentual")
        private Double percentageDifference;

        @Schema(description = "Precisión de estimaciones (%)")
        private Double estimationAccuracy;
    }
}