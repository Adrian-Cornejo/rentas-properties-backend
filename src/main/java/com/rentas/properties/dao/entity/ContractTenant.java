package com.rentas.properties.dao.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad ContractTenant - Relación N:M entre Contratos y Arrendatarios
 * Permite múltiples responsables por contrato (esposo/esposa, aval)
 */
@Entity
@Table(name = "contract_tenants",
        indexes = {
                @Index(name = "idx_contract_tenants_contract", columnList = "contract_id"),
                @Index(name = "idx_contract_tenants_tenant", columnList = "tenant_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "unique_contract_tenant", columnNames = {"contract_id", "tenant_id"})
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContractTenant {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    // Relación Many-to-One con Contract
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false, foreignKey = @ForeignKey(name = "fk_contract_tenant_contract"))
    private Contract contract;

    // Relación Many-to-One con Tenant
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false, foreignKey = @ForeignKey(name = "fk_contract_tenant_tenant"))
    private Tenant tenant;

    @Column(name = "is_primary")
    @Builder.Default
    private Boolean isPrimary = true; // Responsable principal del pago

    @Size(max = 100)
    @Column(name = "relationship", length = 100)
    private String relationship; // Ej: "Esposo", "Esposa", "Aval", "Co-arrendatario"

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    // Métodos de utilidad
    public boolean isResponsible() {
        return Boolean.TRUE.equals(isPrimary);
    }

    public String getTenantFullName() {
        return tenant != null ? tenant.getFullName() : null;
    }

    public String getTenantPhone() {
        return tenant != null ? tenant.getPhone() : null;
    }

    public String getDisplayRelationship() {
        if (relationship != null && !relationship.isEmpty()) {
            return relationship;
        }
        return isPrimary ? "Titular" : "Co-titular";
    }
}