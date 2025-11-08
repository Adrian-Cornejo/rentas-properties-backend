package com.rentas.properties.dao.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad Notification - Log de notificaciones enviadas
 * Maneja SMS y WhatsApp con Twilio
 */
@Entity
@Table(name = "notifications", indexes = {
        @Index(name = "idx_notifications_recipient", columnList = "recipient_type, recipient_id"),
        @Index(name = "idx_notifications_status", columnList = "status"),
        @Index(name = "idx_notifications_type", columnList = "notification_type"),
        @Index(name = "idx_notifications_created", columnList = "created_at")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    // Destinatario
    @NotBlank(message = "El tipo de destinatario es obligatorio")
    @Size(max = 50)
    @Column(name = "recipient_type", nullable = false, length = 50)
    private String recipientType; // TENANT, USER

    @Column(name = "recipient_id")
    private UUID recipientId;

    @NotBlank(message = "El teléfono del destinatario es obligatorio")
    @Size(max = 20)
    @Column(name = "recipient_phone", nullable = false, length = 20)
    private String recipientPhone;

    // Contenido
    @NotBlank(message = "El tipo de notificación es obligatorio")
    @Size(max = 50)
    @Column(name = "notification_type", nullable = false, length = 50)
    private String notificationType; // PAYMENT_REMINDER, CONTRACT_EXPIRY, MAINTENANCE_ALERT

    @NotBlank(message = "El título es obligatorio")
    @Size(max = 255)
    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @NotBlank(message = "El mensaje es obligatorio")
    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    // Canal
    @NotBlank(message = "El canal es obligatorio")
    @Size(max = 50)
    @Column(name = "channel", nullable = false, length = 50)
    private String channel; // SMS, WHATSAPP, EMAIL

    // Estado
    @Size(max = 50)
    @Column(name = "status", length = 50)
    @Builder.Default
    private String status = "PENDING"; // PENDING, SENT, FAILED, DELIVERED

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    // Referencias
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_contract_id", foreignKey = @ForeignKey(name = "fk_notification_contract"))
    private Contract relatedContract;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_payment_id", foreignKey = @ForeignKey(name = "fk_notification_payment"))
    private Payment relatedPayment;

    // Provider info (Twilio)
    @Size(max = 255)
    @Column(name = "provider_message_id", length = 255)
    private String providerMessageId;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private UUID createdBy;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    // Métodos de utilidad
    public boolean isPending() {
        return "PENDING".equalsIgnoreCase(this.status);
    }

    public boolean isSent() {
        return "SENT".equalsIgnoreCase(this.status);
    }

    public boolean isFailed() {
        return "FAILED".equalsIgnoreCase(this.status);
    }

    public boolean isDelivered() {
        return "DELIVERED".equalsIgnoreCase(this.status);
    }

    public boolean isSMS() {
        return "SMS".equalsIgnoreCase(this.channel);
    }

    public boolean isWhatsApp() {
        return "WHATSAPP".equalsIgnoreCase(this.channel);
    }

    public boolean isEmail() {
        return "EMAIL".equalsIgnoreCase(this.channel);
    }

    public boolean isPaymentReminder() {
        return "PAYMENT_REMINDER".equalsIgnoreCase(this.notificationType);
    }

    public boolean isContractExpiry() {
        return "CONTRACT_EXPIRY".equalsIgnoreCase(this.notificationType);
    }

    public boolean isMaintenanceAlert() {
        return "MAINTENANCE_ALERT".equalsIgnoreCase(this.notificationType);
    }

    public void markAsSent(String messageId) {
        this.status = "SENT";
        this.sentAt = LocalDateTime.now();
        this.providerMessageId = messageId;
    }

    public void markAsDelivered() {
        this.status = "DELIVERED";
        this.deliveredAt = LocalDateTime.now();
    }

    public void markAsFailed(String error) {
        this.status = "FAILED";
        this.errorMessage = error;
    }

    public boolean hasError() {
        return errorMessage != null && !errorMessage.isEmpty();
    }

    public long getMinutesSinceSent() {
        if (sentAt != null) {
            return java.time.Duration.between(sentAt, LocalDateTime.now()).toMinutes();
        }
        return 0;
    }

    public boolean isRecipientTenant() {
        return "TENANT".equalsIgnoreCase(this.recipientType);
    }

    public boolean isRecipientUser() {
        return "USER".equalsIgnoreCase(this.recipientType);
    }
}