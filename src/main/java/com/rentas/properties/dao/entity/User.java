package com.rentas.properties.dao.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad User - Usuarios del sistema (familia/administradores)
 * Maneja autenticación y autorización
 */
@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_users_email", columnList = "email"),
        @Index(name = "idx_users_is_active", columnList = "is_active"),
        @Index(name = "idx_users_organization", columnList = "organization_id"),
        @Index(name = "idx_users_account_status", columnList = "account_status")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Email debe ser válido")
    @Size(max = 255)
    @Column(name = "email", unique = true, nullable = false, length = 255)
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(max = 255)
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @NotBlank(message = "El nombre completo es obligatorio")
    @Size(max = 255)
    @Column(name = "full_name", nullable = false, length = 255)
    private String fullName;

    @Size(max = 20)
    @Column(name = "phone", length = 20)
    private String phone;

    @Size(max = 50)
    @Column(name = "role", length = 50)
    @Builder.Default
    private String role = "USER"; // ADMIN, USER

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    // ========== NUEVOS CAMPOS MULTI-TENANT ==========

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", foreignKey = @ForeignKey(name = "fk_user_organization"))
    private Organization organization;

    @Column(name = "organization_joined_at")
    private LocalDateTime organizationJoinedAt;

    @Size(max = 50)
    @Column(name = "account_status", length = 50)
    @Builder.Default
    private String accountStatus = "pending"; // pending, active, suspended

    // ================================================

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Métodos de utilidad

    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(this.role);
    }

    public boolean isActiveUser() {
        return Boolean.TRUE.equals(this.isActive);
    }

    // ========== NUEVOS MÉTODOS MULTI-TENANT ==========

    public boolean hasOrganization() {
        return organization != null;
    }

    public boolean isPending() {
        return "pending".equalsIgnoreCase(this.accountStatus);
    }

    public boolean isAccountActive() {
        return "active".equalsIgnoreCase(this.accountStatus);
    }

    public boolean isSuspended() {
        return "suspended".equalsIgnoreCase(this.accountStatus);
    }

    public UUID getOrganizationId() {
        return organization != null ? organization.getId() : null;
    }

    public String getOrganizationName() {
        return organization != null ? organization.getName() : "Sin organización";
    }

    public boolean isOrganizationOwner() {
        return organization != null && organization.getOwner() != null
                && organization.getOwner().getId().equals(this.id);
    }

    public void joinOrganization(Organization org) {
        this.organization = org;
        this.organizationJoinedAt = LocalDateTime.now();
        this.accountStatus = "active";
    }

    public void leaveOrganization() {
        this.organization = null;
        this.organizationJoinedAt = null;
        this.accountStatus = "pending";
    }

    public long getDaysSinceJoined() {
        if (organizationJoinedAt != null) {
            return java.time.temporal.ChronoUnit.DAYS.between(organizationJoinedAt, LocalDateTime.now());
        }
        return 0;
    }
}