package com.rentas.properties.dao.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad Payment - Registro de pagos mensuales (renta + agua)
 * Maneja pagos, recargos por mora y estados
 */
@Entity
@Table(name = "payments", indexes = {
        @Index(name = "idx_payments_contract", columnList = "contract_id"),
        @Index(name = "idx_payments_status", columnList = "status"),
        @Index(name = "idx_payments_dates", columnList = "payment_date, due_date"),
        @Index(name = "idx_payments_period", columnList = "period_year, period_month"),
        @Index(name = "idx_payments_type", columnList = "payment_type")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    // Relación Many-to-One con Contract
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false, foreignKey = @ForeignKey(name = "fk_payment_contract"))
    private Contract contract;

    // Tipo de pago
    @NotBlank(message = "El tipo de pago es obligatorio")
    @Size(max = 50)
    @Column(name = "payment_type", nullable = false, length = 50)
    private String paymentType; // RENTA, AGUA, DEPOSITO, ADELANTO

    // Fechas
    @NotNull(message = "La fecha de pago es obligatoria")
    @Column(name = "payment_date", nullable = false)
    private LocalDate paymentDate;

    @NotNull(message = "La fecha de vencimiento es obligatoria")
    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @NotNull(message = "El mes del periodo es obligatorio")
    @Min(value = 1, message = "El mes debe estar entre 1 y 12")
    @Max(value = 12, message = "El mes debe estar entre 1 y 12")
    @Column(name = "period_month", nullable = false)
    private Integer periodMonth;

    @NotNull(message = "El año del periodo es obligatorio")
    @Min(value = 2020, message = "El año debe ser válido")
    @Column(name = "period_year", nullable = false)
    private Integer periodYear;

    // Montos
    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false)
    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "late_fee", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal lateFee = BigDecimal.ZERO;

    @NotNull(message = "El monto total es obligatorio")
    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    // Estado
    @Size(max = 50)
    @Column(name = "status", length = 50)
    @Builder.Default
    private String status = "PENDIENTE"; // PENDIENTE, PAGADO, ATRASADO, PARCIAL

    // Método de pago
    @Size(max = 50)
    @Column(name = "payment_method", length = 50)
    private String paymentMethod; // EFECTIVO, TRANSFERENCIA, TARJETA

    @Size(max = 100)
    @Column(name = "reference_number", length = 100)
    private String referenceNumber;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    // Usuario que cobró
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collected_by", foreignKey = @ForeignKey(name = "fk_payment_collector"))
    private User collectedBy;

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
        // Calcular total automáticamente
        if (totalAmount == null && amount != null) {
            totalAmount = amount.add(lateFee != null ? lateFee : BigDecimal.ZERO);
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        // Recalcular total si cambió el monto o recargo
        if (amount != null) {
            totalAmount = amount.add(lateFee != null ? lateFee : BigDecimal.ZERO);
        }
    }

    // Métodos de utilidad
    public boolean isPending() {
        return "PENDIENTE".equalsIgnoreCase(this.status);
    }

    public boolean isPaid() {
        return "PAGADO".equalsIgnoreCase(this.status);
    }

    public boolean isOverdue() {
        return "ATRASADO".equalsIgnoreCase(this.status) ||
                (isPending() && dueDate != null && LocalDate.now().isAfter(dueDate));
    }

    public boolean isPartial() {
        return "PARCIAL".equalsIgnoreCase(this.status);
    }

    public long getDaysOverdue() {
        if (dueDate != null && LocalDate.now().isAfter(dueDate)) {
            return java.time.temporal.ChronoUnit.DAYS.between(dueDate, LocalDate.now());
        }
        return 0;
    }

    public boolean hasLateFee() {
        return lateFee != null && lateFee.compareTo(BigDecimal.ZERO) > 0;
    }

    public String getPeriodDescription() {
        String[] months = {"Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
                "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};
        if (periodMonth >= 1 && periodMonth <= 12) {
            return months[periodMonth - 1] + " " + periodYear;
        }
        return periodMonth + "/" + periodYear;
    }

    public void markAsPaid(User collector, String method, String reference) {
        this.status = "PAGADO";
        this.paidAt = LocalDateTime.now();
        this.collectedBy = collector;
        this.paymentMethod = method;
        this.referenceNumber = reference;
    }

    public void addLateFee(BigDecimal fee) {
        this.lateFee = (this.lateFee != null ? this.lateFee : BigDecimal.ZERO).add(fee);
        this.totalAmount = this.amount.add(this.lateFee);
        if (this.status.equals("PENDIENTE")) {
            this.status = "ATRASADO";
        }
    }

    public boolean isRentPayment() {
        return "RENTA".equalsIgnoreCase(this.paymentType);
    }

    public boolean isWaterPayment() {
        return "AGUA".equalsIgnoreCase(this.paymentType);
    }

    public boolean isDepositPayment() {
        return "DEPOSITO".equalsIgnoreCase(this.paymentType);
    }

    public boolean isAdvancePayment() {
        return "ADELANTO".equalsIgnoreCase(this.paymentType);
    }
}
