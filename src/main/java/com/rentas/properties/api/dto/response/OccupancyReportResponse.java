package com.rentas.properties.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response del reporte de ocupación")
public class OccupancyReportResponse {

    @Schema(description = "Fecha de inicio del periodo")
    private LocalDate startDate;

    @Schema(description = "Fecha de fin del periodo")
    private LocalDate endDate;

    @Schema(description = "Resumen de ocupación")
    private OccupancySummary summary;

    @Schema(description = "Ocupación mensual")
    private List<MonthlyOccupancy> monthlyOccupancy;

    @Schema(description = "Historial de ocupación por propiedad")
    private List<PropertyOccupancy> propertyOccupancy;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OccupancySummary {
        @Schema(description = "Total de propiedades")
        private Integer totalProperties;

        @Schema(description = "Propiedades rentadas")
        private Integer rentedProperties;

        @Schema(description = "Propiedades disponibles")
        private Integer availableProperties;

        @Schema(description = "Propiedades en mantenimiento")
        private Integer maintenanceProperties;

        @Schema(description = "Tasa de ocupación promedio (%)")
        private Double occupancyRate;

        @Schema(description = "Días promedio para rentar una propiedad")
        private Integer averageDaysToRent;

        @Schema(description = "Rotación de inquilinos en el periodo")
        private Integer tenantTurnover;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyOccupancy {
        @Schema(description = "Nombre del mes")
        private String month;

        @Schema(description = "Año")
        private Integer year;

        @Schema(description = "Propiedades rentadas")
        private Integer rented;

        @Schema(description = "Propiedades disponibles")
        private Integer available;

        @Schema(description = "Tasa de ocupación (%)")
        private Double occupancyRate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PropertyOccupancy {
        @Schema(description = "Código de la propiedad")
        private String propertyCode;

        @Schema(description = "Dirección de la propiedad")
        private String address;

        @Schema(description = "Tipo de propiedad")
        private String propertyType;

        @Schema(description = "Estado actual")
        private String currentStatus;

        @Schema(description = "Días ocupada en el periodo")
        private Integer daysOccupied;

        @Schema(description = "Días disponible en el periodo")
        private Integer daysAvailable;

        @Schema(description = "Tasa de ocupación (%)")
        private Double occupancyRate;

        @Schema(description = "Número de contratos en el periodo")
        private Integer contractsCount;
    }
}