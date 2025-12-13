package com.rentas.properties.dao.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entidad Organization - Organizaciones/Familias para sistema Multi-Tenant
 * Cada organización tiene su propio conjunto de propiedades, usuarios, contratos, etc.
 */
@Entity
@Table(name = "organizations", indexes = {
        @Index(name = "idx_organizations_invitation_code", columnList = "invitation_code"),
        @Index(name = "idx_organizations_owner", columnList = "owner_id"),
        @Index(name = "idx_organizations_subscription_status", columnList = "subscription_status"),
        @Index(name = "idx_organizations_is_active", columnList = "is_active")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Organization {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    // Información básica
    @NotBlank(message = "El nombre de la organización es obligatorio")
    @Size(max = 255)
    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // Personalización (Marca Blanca)
    @Column(name = "logo_url", columnDefinition = "TEXT")
    private String logoUrl;

    @Size(max = 255)
    @Column(name = "logo_public_id", length = 255)
    private String logoPublicId;

    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "El color debe ser un hex válido (#RRGGBB)")
    @Size(max = 7)
    @Column(name = "primary_color", length = 7)
    @Builder.Default
    private String primaryColor = "#3B82F6";

    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "El color debe ser un hex válido (#RRGGBB)")
    @Size(max = 7)
    @Column(name = "secondary_color", length = 7)
    @Builder.Default
    private String secondaryColor = "#10B981";

    // Código de invitación
    @NotBlank(message = "El código de invitación es obligatorio")
    @Size(max = 8)
    @Column(name = "invitation_code", unique = true, nullable = false, length = 8)
    private String invitationCode; // Formato: ABC-12D3

    @Column(name = "code_is_reusable")
    @Builder.Default
    private Boolean codeIsReusable = true;

    // Owner (Admin principal)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", foreignKey = @ForeignKey(name = "fk_organization_owner"))
    private User owner;

    // Límites según plan
    @Column(name = "max_users")
    @Builder.Default
    private Integer maxUsers = 3;

    @Column(name = "max_properties")
    @Builder.Default
    private Integer maxProperties = 3;

    @Column(name = "current_users_count")
    @Builder.Default
    private Integer currentUsersCount = 0;

    @Column(name = "current_properties_count")
    @Builder.Default
    private Integer currentPropertiesCount = 0;

    // Subscription
    @Size(max = 50)
    @Column(name = "subscription_status", length = 50)
    @Builder.Default
    private String subscriptionStatus = "trial"; // trial, active, suspended, cancelled

    @Size(max = 50)
    @Column(name = "subscription_plan", length = 50)
    @Builder.Default
    private String subscriptionPlan = "free"; // free, basic, pro, enterprise

    @Column(name = "trial_ends_at")
    private LocalDateTime trialEndsAt;

    @Column(name = "subscription_started_at")
    private LocalDateTime subscriptionStartedAt;

    @Column(name = "subscription_ends_at")
    private LocalDateTime subscriptionEndsAt;

    // Estado
    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    // Auditoría
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "notification_enabled")
    private Boolean notificationEnabled;

    @Column(name = "notification_channel", length = 20)
    private String notificationChannel; // SMS, WHATSAPP, BOTH

    @Column(name = "notifications_sent_this_month")
    private Integer notificationsSentThisMonth;

    @Column(name = "notification_limit")
    private Integer notificationLimit;

    @Column(name = "last_notification_reset")
    private LocalDate lastNotificationReset;

    @Column(name = "admin_notifications")
    private Boolean adminNotifications;

    // Relaciones
    @OneToMany(mappedBy = "organization", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<User> users = new ArrayList<>();

    @OneToMany(mappedBy = "organization", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Location> locations = new ArrayList<>();

    @OneToMany(mappedBy = "organization", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Property> properties = new ArrayList<>();

    @OneToMany(mappedBy = "organization", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Tenant> tenants = new ArrayList<>();

    @OneToMany(mappedBy = "organization", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Contract> contracts = new ArrayList<>();

    @OneToMany(mappedBy = "organization", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<MaintenanceRecord> maintenanceRecords = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        updatedAt = LocalDateTime.now();

        if (notificationEnabled == null) {
            notificationEnabled = false;
        }
        if (notificationsSentThisMonth == null) {
            notificationsSentThisMonth = 0;
        }
        if (notificationLimit == null) {
            notificationLimit = 0;
        }
        if (adminNotifications == null) {
            adminNotifications = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Métodos de utilidad

    public boolean isActive() {
        return Boolean.TRUE.equals(isActive);
    }

    public boolean isTrial() {
        return "trial".equalsIgnoreCase(this.subscriptionStatus);
    }

    public boolean isSubscriptionActive() {
        return "active".equalsIgnoreCase(this.subscriptionStatus);
    }

    public boolean isSuspended() {
        return "suspended".equalsIgnoreCase(this.subscriptionStatus);
    }

    public boolean isCancelled() {
        return "cancelled".equalsIgnoreCase(this.subscriptionStatus);
    }

    public boolean isTrialExpired() {
        return isTrial() && trialEndsAt != null && LocalDateTime.now().isAfter(trialEndsAt);
    }

    public boolean isSubscriptionExpired() {
        return subscriptionEndsAt != null && LocalDateTime.now().isAfter(subscriptionEndsAt);
    }

    public boolean hasReachedUserLimit() {
        return currentUsersCount != null && maxUsers != null && currentUsersCount >= maxUsers;
    }

    public boolean hasReachedPropertyLimit() {
        return currentPropertiesCount != null && maxProperties != null && currentPropertiesCount >= maxProperties;
    }

    public boolean canAddUser() {
        return !hasReachedUserLimit() && isSubscriptionActive() && isActive();
    }

    public boolean canAddProperty() {
        return !hasReachedPropertyLimit() && isSubscriptionActive() && isActive();
    }

    public void incrementUsersCount() {
        this.currentUsersCount = (this.currentUsersCount != null ? this.currentUsersCount : 0) + 1;
    }

    public void decrementUsersCount() {
        if (this.currentUsersCount != null && this.currentUsersCount > 0) {
            this.currentUsersCount--;
        }
    }

    public void incrementPropertiesCount() {
        this.currentPropertiesCount = (this.currentPropertiesCount != null ? this.currentPropertiesCount : 0) + 1;
    }

    public void decrementPropertiesCount() {
        if (this.currentPropertiesCount != null && this.currentPropertiesCount > 0) {
            this.currentPropertiesCount--;
        }
    }

    public boolean hasLogo() {
        return logoUrl != null && !logoUrl.isEmpty();
    }

    public String getLogoUrlThumbnail() {
        if (logoUrl != null && logoUrl.contains("cloudinary")) {
            return logoUrl.replace("/upload/", "/upload/c_scale,w_200/");
        }
        return logoUrl;
    }

    public long getDaysUntilTrialEnds() {
        if (trialEndsAt != null) {
            return java.time.temporal.ChronoUnit.DAYS.between(LocalDateTime.now(), trialEndsAt);
        }
        return 0;
    }

    public long getDaysUntilSubscriptionEnds() {
        if (subscriptionEndsAt != null) {
            return java.time.temporal.ChronoUnit.DAYS.between(LocalDateTime.now(), subscriptionEndsAt);
        }
        return 0;
    }


    public String getOwnerName() {
        return owner != null ? owner.getFullName() : "Sin dueño";
    }

    public String getOwnerEmail() {
        return owner != null ? owner.getEmail() : "N/A";
    }

    public boolean isFreePlan() {
        return "free".equalsIgnoreCase(this.subscriptionPlan);
    }

    public boolean isBasicPlan() {
        return "basic".equalsIgnoreCase(this.subscriptionPlan);
    }

    public boolean isProPlan() {
        return "pro".equalsIgnoreCase(this.subscriptionPlan);
    }

    public boolean isEnterprisePlan() {
        return "enterprise".equalsIgnoreCase(this.subscriptionPlan);
    }
}