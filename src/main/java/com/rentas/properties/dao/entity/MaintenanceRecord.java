package com.rentas.properties.dao.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entidad MaintenanceRecord - Registro de mantenimiento y reparaciones
 * Incluye mantenimiento preventivo, correctivo y emergencias
 */
@Entity
@Table(name = "maintenance_records", indexes = {
        @Index(name = "idx_maintenance_property", columnList = "property_id"),
        @Index(name = "idx_maintenance_contract", columnList = "contract_id"),
        @Index(name = "idx_maintenance_status", columnList = "status"),
        @Index(name = "idx_maintenance_date", columnList = "maintenance_date"),
        @Index(name = "idx_maintenance_type", columnList = "maintenance_type"),
        @Index(name = "idx_maintenance_organization", columnList = "organization_id")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaintenanceRecord extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    // ========== NUEVO CAMPO MULTI-TENANT ==========
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false, foreignKey = @ForeignKey(name = "fk_maintenance_organization"))
    private Organization organization;
    // ==============================================

    // Relación Many-to-One con Property
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false, foreignKey = @ForeignKey(name = "fk_maintenance_property"))
    private Property property;

    // Relación Many-to-One con Contract (puede ser null si la propiedad está disponible)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", foreignKey = @ForeignKey(name = "fk_maintenance_contract"))
    private Contract contract;

    // Detalles
    @NotBlank(message = "El título es obligatorio")
    @Size(max = 255)
    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @NotBlank(message = "La descripción es obligatoria")
    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @NotBlank(message = "El tipo de mantenimiento es obligatorio")
    @Size(max = 50)
    @Column(name = "maintenance_type", nullable = false, length = 50)
    private String maintenanceType; // PREVENTIVO, CORRECTIVO, EMERGENCIA

    @Size(max = 50)
    @Column(name = "category", length = 50)
    private String category; // PLOMERIA, ELECTRICIDAD, PINTURA, LIMPIEZA, etc.

    // Fechas
    @NotNull(message = "La fecha de mantenimiento es obligatoria")
    @Column(name = "maintenance_date", nullable = false)
    private LocalDate maintenanceDate;

    @Column(name = "completed_date")
    private LocalDate completedDate;

    // Costos
    @Column(name = "estimated_cost", precision = 10, scale = 2)
    private BigDecimal estimatedCost;

    @Column(name = "actual_cost", precision = 10, scale = 2)
    private BigDecimal actualCost;

    // Estado
    @Size(max = 50)
    @Column(name = "status", length = 50)
    @Builder.Default
    private String status = "PENDIENTE"; // PENDIENTE, EN_PROCESO, COMPLETADO, CANCELADO

    // Responsable
    @Size(max = 255)
    @Column(name = "assigned_to", length = 255)
    private String assignedTo; // Nombre del técnico/responsable

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    // Relación One-to-Many con MaintenanceImages
    @OneToMany(mappedBy = "maintenanceRecord", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<MaintenanceImage> images = new ArrayList<>();

    // Métodos de utilidad existentes...
    public void addImage(MaintenanceImage image) {
        images.add(image);
        image.setMaintenanceRecord(this);
    }

    public void removeImage(MaintenanceImage image) {
        images.remove(image);
        image.setMaintenanceRecord(null);
    }

    public boolean isPending() {
        return "PENDIENTE".equalsIgnoreCase(this.status);
    }

    public boolean isInProgress() {
        return "EN_PROCESO".equalsIgnoreCase(this.status);
    }

    public boolean isCompleted() {
        return "COMPLETADO".equalsIgnoreCase(this.status);
    }

    public boolean isCancelled() {
        return "CANCELADO".equalsIgnoreCase(this.status);
    }

    public boolean isPreventive() {
        return "PREVENTIVO".equalsIgnoreCase(this.maintenanceType);
    }

    public boolean isCorrective() {
        return "CORRECTIVO".equalsIgnoreCase(this.maintenanceType);
    }

    public boolean isEmergency() {
        return "EMERGENCIA".equalsIgnoreCase(this.maintenanceType);
    }

    public boolean hasEstimatedCost() {
        return estimatedCost != null && estimatedCost.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean hasActualCost() {
        return actualCost != null && actualCost.compareTo(BigDecimal.ZERO) > 0;
    }

    public BigDecimal getCostVariance() {
        if (estimatedCost != null && actualCost != null) {
            return actualCost.subtract(estimatedCost);
        }
        return BigDecimal.ZERO;
    }

    public boolean isOverBudget() {
        BigDecimal variance = getCostVariance();
        return variance.compareTo(BigDecimal.ZERO) > 0;
    }

    public long getDurationInDays() {
        if (maintenanceDate != null && completedDate != null) {
            return java.time.temporal.ChronoUnit.DAYS.between(maintenanceDate, completedDate);
        }
        return 0;
    }

    public boolean isOverdue() {
        return !isCompleted() && maintenanceDate != null &&
                LocalDate.now().isAfter(maintenanceDate);
    }

    public int getTotalImages() {
        return images != null ? images.size() : 0;
    }

    public List<MaintenanceImage> getBeforeImages() {
        return images != null
                ? images.stream().filter(img -> "ANTES".equals(img.getImageType())).toList()
                : new ArrayList<>();
    }

    public List<MaintenanceImage> getAfterImages() {
        return images != null
                ? images.stream().filter(img -> "DESPUES".equals(img.getImageType())).toList()
                : new ArrayList<>();
    }

    public void markAsCompleted(LocalDate completionDate, BigDecimal finalCost) {
        this.status = "COMPLETADO";
        this.completedDate = completionDate != null ? completionDate : LocalDate.now();
        if (finalCost != null) {
            this.actualCost = finalCost;
        }
    }

    public void startWork() {
        if (isPending()) {
            this.status = "EN_PROCESO";
        }
    }

    public void cancel(String reason) {
        this.status = "CANCELADO";
        if (this.notes != null) {
            this.notes += "\nMotivo de cancelación: " + reason;
        } else {
            this.notes = "Motivo de cancelación: " + reason;
        }
    }

    public UUID getOrganizationId() {
        return organization != null ? organization.getId() : null;
    }

    public String getOrganizationName() {
        return organization != null ? organization.getName() : "N/A";
    }

    public boolean belongsToOrganization(UUID organizationId) {
        return organization != null && organization.getId().equals(organizationId);
    }
}