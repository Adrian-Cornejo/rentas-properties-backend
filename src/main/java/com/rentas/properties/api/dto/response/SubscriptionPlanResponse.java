// src/main/java/com/rentas/properties/api/dto/response/SubscriptionPlanResponse.java
package com.rentas.properties.api.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Información completa de un plan de suscripción")
public class SubscriptionPlanResponse {

    @Schema(description = "ID del plan")
    private String id;

    @Schema(description = "Código único del plan", example = "PROFESIONAL")
    private String planCode;

    @Schema(description = "Nombre del plan", example = "Profesional")
    private String planName;

    @Schema(description = "Descripción del plan")
    private String planDescription;

    // Pricing
    @Schema(description = "Precio mensual", example = "799.00")
    private BigDecimal monthlyPrice;

    @Schema(description = "Precio anual (con descuento)", example = "7990.00")
    private BigDecimal annualPrice;

    @Schema(description = "Moneda", example = "MXN")
    private String currency;

    @Schema(description = "Días de prueba gratis", example = "30")
    private Integer trialDays;

    // Límites
    @Schema(description = "Máximo de propiedades", example = "50")
    private Integer maxProperties;

    @Schema(description = "Máximo de usuarios (-1 = ilimitado)", example = "10")
    private Integer maxUsers;

    @Schema(description = "Máximo de contratos activos", example = "50")
    private Integer maxActiveContracts;

    @Schema(description = "Límite de almacenamiento en MB", example = "10240")
    private Integer storageLimitMb;

    @Schema(description = "Imágenes por propiedad", example = "15")
    private Integer imagesPerProperty;

    @Schema(description = "Días de historial de reportes (-1 = ilimitado)", example = "365")
    private Integer reportHistoryDays;

    // Notificaciones
    @Schema(description = "Tiene notificaciones automáticas")
    private Boolean hasNotifications;

    @Schema(description = "Canales de notificación disponibles", example = "BOTH")
    private String notificationChannels;

    @Schema(description = "Límite mensual de notificaciones (-1 = ilimitado)", example = "500")
    private Integer monthlyNotificationLimit;

    @Schema(description = "Tiene recordatorios de atraso")
    private Boolean hasLateReminders;

    @Schema(description = "Tiene resumen diario para admin")
    private Boolean hasAdminDigest;

    // Mantenimiento
    @Schema(description = "Programación de mantenimiento")
    private Boolean hasMaintenanceScheduling;

    @Schema(description = "Permite fotos en mantenimiento")
    private Boolean hasMaintenancePhotos;

    // Reportes
    @Schema(description = "Reportes avanzados")
    private Boolean hasAdvancedReports;

    @Schema(description = "Exportación de datos")
    private Boolean hasDataExport;

    @Schema(description = "Reportes en PDF")
    private Boolean hasPdfReports;

    // Funcionalidades avanzadas
    @Schema(description = "Acceso a API")
    private Boolean hasApiAccess;

    @Schema(description = "White label")
    private Boolean hasWhiteLabel;

    @Schema(description = "Nivel de white label", example = "BASIC")
    private String whiteLabelLevel;

    @Schema(description = "Multi-moneda")
    private Boolean hasMultiCurrency;

    @Schema(description = "Gestión documental")
    private Boolean hasDocumentManagement;

    @Schema(description = "Firma electrónica")
    private Boolean hasESignature;

    @Schema(description = "Portal de inquilinos")
    private Boolean hasTenantPortal;

    @Schema(description = "App móvil")
    private Boolean hasMobileApp;

    @Schema(description = "Integraciones")
    private Boolean hasIntegrations;

    // Soporte
    @Schema(description = "Nivel de soporte", example = "priority")
    private String supportLevel;

    @Schema(description = "Tiempo de respuesta de soporte en horas", example = "18")
    private Integer supportResponseHours;

    @Schema(description = "Incluye onboarding")
    private Boolean hasOnboarding;

    @Schema(description = "Tiene account manager")
    private Boolean hasAccountManager;

    // Display
    @Schema(description = "Orden de visualización", example = "3")
    private Integer displayOrder;

    @Schema(description = "Es el plan más popular")
    private Boolean isPopular;

    @Schema(description = "Es plan personalizado")
    private Boolean isCustom;

    // Computed fields
    @Schema(description = "Usuarios ilimitados")
    private Boolean unlimitedUsers;

    @Schema(description = "Notificaciones ilimitadas")
    private Boolean unlimitedNotifications;

    @Schema(description = "Historial ilimitado")
    private Boolean unlimitedHistory;

    @Schema(description = "Permite subir imágenes")
    private Boolean allowsImages;

    @Schema(description = "Ahorro anual en porcentaje", example = "16.67")
    private BigDecimal annualSavingsPercentage;

    // ============================================
    // DTOs ANIDADOS
    // ============================================

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FeatureDto {
        private String category;
        private String name;
        private String description;
        private Boolean included;
        private Boolean highlight;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Comparación entre dos planes")
    public static class PlanComparisonResponse {
        @Schema(description = "Plan actual")
        private SubscriptionPlanResponse currentPlan;

        @Schema(description = "Plan objetivo")
        private SubscriptionPlanResponse targetPlan;

        @Schema(description = "Es un upgrade (mejora)")
        private Boolean isUpgrade;

        @Schema(description = "Diferencia de precio mensual")
        private BigDecimal priceDifference;

        @Schema(description = "Diferencia porcentual de precio")
        private BigDecimal priceDifferencePercentage;

        @Schema(description = "Mejoras obtenidas")
        private List<String> improvements;

        @Schema(description = "Funcionalidades que se pierden")
        private List<String> downgrades;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Estadísticas de uso de un plan")
    public static class PlanStatsResponse {
        @Schema(description = "ID del plan")
        private String planId;

        @Schema(description = "Código del plan")
        private String planCode;

        @Schema(description = "Nombre del plan")
        private String planName;

        @Schema(description = "Número de organizaciones usando este plan")
        private Long organizationCount;

        @Schema(description = "Porcentaje del total de organizaciones")
        private BigDecimal marketShare;

        @Schema(description = "Ingreso mensual proyectado")
        private BigDecimal projectedMonthlyRevenue;

        @Schema(description = "Ingreso anual proyectado")
        private BigDecimal projectedAnnualRevenue;
    }
}