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
 * Entidad Contract - Contratos de arrendamiento
 * Maneja contratos de 6 meses con adelanto y depósito
 */
@Entity
@Table(name = "contracts", indexes = {
        @Index(name = "idx_contracts_property", columnList = "property_id"),
        @Index(name = "idx_contracts_status", columnList = "status"),
        @Index(name = "idx_contracts_dates", columnList = "start_date, end_date"),
        @Index(name = "idx_contracts_number", columnList = "contract_number")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Contract extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    // Relación Many-to-One con Property
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false, foreignKey = @ForeignKey(name = "fk_contract_property"))
    private Property property;

    @NotBlank(message = "El número de contrato es obligatorio")
    @Size(max = 50)
    @Column(name = "contract_number", unique = true, nullable = false, length = 50)
    private String contractNumber;

    // Fechas
    @NotNull(message = "La fecha de inicio es obligatoria")
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @NotNull(message = "La fecha de fin es obligatoria")
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "signed_date")
    private LocalDate signedDate;

    // Montos
    @NotNull(message = "La renta mensual es obligatoria")
    @DecimalMin(value = "0.0", inclusive = false)
    @Column(name = "monthly_rent", nullable = false, precision = 10, scale = 2)
    private BigDecimal monthlyRent;

    @Column(name = "water_fee", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal waterFee = new BigDecimal("105.00");

    @NotNull(message = "El pago adelantado es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false)
    @Column(name = "advance_payment", nullable = false, precision = 10, scale = 2)
    private BigDecimal advancePayment;

    @NotNull(message = "El depósito es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false)
    @Column(name = "deposit_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal depositAmount;

    @Column(name = "deposit_paid")
    @Builder.Default
    private Boolean depositPaid = false;

    @Column(name = "deposit_payment_deadline")
    private LocalDate depositPaymentDeadline;

    // Estado del depósito
    @Size(max = 50)
    @Column(name = "deposit_status", length = 50)
    @Builder.Default
    private String depositStatus = "PENDIENTE"; // PENDIENTE, PAGADO, RETENIDO, DEVUELTO, USADO_REPARACIONES

    @Column(name = "deposit_return_amount", precision = 10, scale = 2)
    private BigDecimal depositReturnAmount;

    @Column(name = "deposit_return_date")
    private LocalDate depositReturnDate;

    @Column(name = "deposit_deduction_reason", columnDefinition = "TEXT")
    private String depositDeductionReason;

    // Estado del contrato
    @Size(max = 50)
    @Column(name = "status", length = 50)
    @Builder.Default
    private String status = "ACTIVO"; // ACTIVO, VENCIDO, RENOVADO, CANCELADO

    // Documentos
    @Column(name = "contract_document_url", columnDefinition = "TEXT")
    private String contractDocumentUrl;

    @Size(max = 255)
    @Column(name = "contract_document_public_id", length = 255)
    private String contractDocumentPublicId;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    // Relaciones
    @OneToMany(mappedBy = "contract", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ContractTenant> contractTenants = new ArrayList<>();

    @OneToMany(mappedBy = "contract", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Payment> payments = new ArrayList<>();

    @OneToMany(mappedBy = "contract", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<MaintenanceRecord> maintenanceRecords = new ArrayList<>();

    // Métodos de utilidad
    public void addTenant(Tenant tenant, boolean isPrimary, String relationship) {
        ContractTenant contractTenant = ContractTenant.builder()
                .contract(this)
                .tenant(tenant)
                .isPrimary(isPrimary)
                .relationship(relationship)
                .build();
        contractTenants.add(contractTenant);
    }

    public Tenant getPrimaryTenant() {
        return contractTenants.stream()
                .filter(ContractTenant::getIsPrimary)
                .map(ContractTenant::getTenant)
                .findFirst()
                .orElse(null);
    }

    public List<Tenant> getAllTenants() {
        return contractTenants.stream()
                .map(ContractTenant::getTenant)
                .toList();
    }

    public boolean isActive() {
        return "ACTIVO".equalsIgnoreCase(this.status);
    }

    public boolean isExpired() {
        return "VENCIDO".equalsIgnoreCase(this.status) ||
                (endDate != null && LocalDate.now().isAfter(endDate));
    }

    public boolean isDepositPending() {
        return "PENDIENTE".equalsIgnoreCase(this.depositStatus);
    }

    public boolean isDepositOverdue() {
        return isDepositPending() && depositPaymentDeadline != null &&
                LocalDate.now().isAfter(depositPaymentDeadline);
    }

    public BigDecimal getTotalMonthlyPayment() {
        return monthlyRent.add(waterFee != null ? waterFee : BigDecimal.ZERO);
    }

    public long getDurationInMonths() {
        if (startDate != null && endDate != null) {
            return java.time.temporal.ChronoUnit.MONTHS.between(startDate, endDate);
        }
        return 0;
    }

    public long getRemainingDays() {
        if (endDate != null) {
            return java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), endDate);
        }
        return 0;
    }

    public boolean hasContractDocument() {
        return contractDocumentUrl != null && !contractDocumentUrl.isEmpty();
    }

    public int getPendingPaymentsCount() {
        return payments != null
                ? (int) payments.stream()
                .filter(p -> "PENDIENTE".equals(p.getStatus()) || "ATRASADO".equals(p.getStatus()))
                .count()
                : 0;
    }

    public BigDecimal getTotalPendingAmount() {
        return payments != null
                ? payments.stream()
                .filter(p -> "PENDIENTE".equals(p.getStatus()) || "ATRASADO".equals(p.getStatus()))
                .map(Payment::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                : BigDecimal.ZERO;
    }
}