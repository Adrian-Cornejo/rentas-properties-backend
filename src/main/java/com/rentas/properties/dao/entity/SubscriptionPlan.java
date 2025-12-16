// src/main/java/com/rentas/properties/dao/entity/SubscriptionPlan.java
package com.rentas.properties.dao.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "subscription_plans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionPlan {

    @Id
    @GeneratedValue
    private UUID id;

    // Identificación
    @Column(name = "plan_code", unique = true, nullable = false, length = 50)
    private String planCode;

    @Column(name = "plan_name", nullable = false, length = 100)
    private String planName;

    @Column(name = "plan_description", columnDefinition = "TEXT")
    private String planDescription;

    // Pricing
    @Column(name = "monthly_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal monthlyPrice;

    @Column(name = "annual_price", precision = 10, scale = 2)
    private BigDecimal annualPrice;

    @Column(length = 3)
    private String currency = "MXN";

    // Trial
    @Column(name = "trial_days")
    private Integer trialDays = 0;

    // Límites de recursos
    @Column(name = "max_properties", nullable = false)
    private Integer maxProperties;

    @Column(name = "max_users", nullable = false)
    private Integer maxUsers; // -1 para ilimitado

    @Column(name = "max_active_contracts", nullable = false)
    private Integer maxActiveContracts;

    @Column(name = "storage_limit_mb", nullable = false)
    private Integer storageLimitMb; // Todo: imágenes + PDFs + documentos

    @Column(name = "images_per_property", nullable = false)
    private Integer imagesPerProperty; // 0 = sin imágenes

    @Column(name = "report_history_days", nullable = false)
    private Integer reportHistoryDays; // -1 para ilimitado

    // Notificaciones
    @Column(name = "has_notifications")
    private Boolean hasNotifications = false;

    @Column(name = "notification_channels", length = 50)
    private String notificationChannels; // NULL, SMS_OR_WHATSAPP, BOTH, UNLIMITED

    @Column(name = "monthly_notification_limit")
    private Integer monthlyNotificationLimit = 0; // -1 = ilimitado, 0 = sin notifs

    @Column(name = "has_late_reminders")
    private Boolean hasLateReminders = false;

    @Column(name = "has_admin_digest")
    private Boolean hasAdminDigest = false;

    // Mantenimiento
    @Column(name = "has_maintenance_scheduling")
    private Boolean hasMaintenanceScheduling = false;

    @Column(name = "has_maintenance_photos")
    private Boolean hasMaintenancePhotos = false; // NUEVO

    // Reportes
    @Column(name = "has_advanced_reports")
    private Boolean hasAdvancedReports = false;

    @Column(name = "has_data_export")
    private Boolean hasDataExport = false;

    @Column(name = "has_pdf_reports")
    private Boolean hasPdfReports = false; // NUEVO

    // Funcionalidades avanzadas
    @Column(name = "has_api_access")
    private Boolean hasApiAccess = false;

    @Column(name = "has_white_label")
    private Boolean hasWhiteLabel = false;

    @Column(name = "white_label_level", length = 20)
    private String whiteLabelLevel; // NULL, BASIC, FULL

    @Column(name = "has_multi_currency")
    private Boolean hasMultiCurrency = false;

    @Column(name = "has_document_management")
    private Boolean hasDocumentManagement = false;

    @Column(name = "has_e_signature")
    private Boolean hasESignature = false;

    @Column(name = "has_tenant_portal")
    private Boolean hasTenantPortal = false;

    @Column(name = "has_mobile_app")
    private Boolean hasMobileApp = false;

    @Column(name = "has_integrations")
    private Boolean hasIntegrations = false;

    // Soporte
    @Column(name = "support_level", length = 50)
    private String supportLevel = "email";

    @Column(name = "support_response_hours")
    private Integer supportResponseHours;

    @Column(name = "has_onboarding")
    private Boolean hasOnboarding = false;

    @Column(name = "has_account_manager")
    private Boolean hasAccountManager = false;

    // Display
    @Column(name = "display_order")
    private Integer displayOrder = 0;

    @Column(name = "is_popular")
    private Boolean isPopular = false;

    @Column(name = "is_custom")
    private Boolean isCustom = false;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relación con features (opcional)
    @OneToMany(mappedBy = "subscriptionPlan", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<SubscriptionFeature> features;

    // Helpers
    public boolean isUnlimitedUsers() {
        return maxUsers != null && maxUsers == -1;
    }

    public boolean isUnlimitedNotifications() {
        return monthlyNotificationLimit != null && monthlyNotificationLimit == -1;
    }

    public boolean isUnlimitedHistory() {
        return reportHistoryDays != null && reportHistoryDays == -1;
    }

    public boolean allowsImages() {
        return imagesPerProperty != null && imagesPerProperty > 0;
    }
}