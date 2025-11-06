package com.rentas.properties.dao.entity;

import com.fasterxml.jackson.databind.JsonNode;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad AuditLog - Log de auditoría de todas las acciones
 * Registra CREATE, UPDATE, DELETE, LOGIN, etc.
 */
@Entity
@Table(name = "audit_log", indexes = {
        @Index(name = "idx_audit_user", columnList = "user_id"),
        @Index(name = "idx_audit_entity", columnList = "entity_type, entity_id"),
        @Index(name = "idx_audit_action", columnList = "action"),
        @Index(name = "idx_audit_created", columnList = "created_at")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    // Relación Many-to-One con User (puede ser null si el usuario fue eliminado)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "fk_audit_user"))
    private User user;

    // Acción realizada
    @NotBlank(message = "La acción es obligatoria")
    @Size(max = 100)
    @Column(name = "action", nullable = false, length = 100)
    private String action; // CREATE, UPDATE, DELETE, LOGIN, LOGOUT, etc.

    @NotBlank(message = "El tipo de entidad es obligatorio")
    @Size(max = 50)
    @Column(name = "entity_type", nullable = false, length = 50)
    private String entityType;

    @Column(name = "entity_id")
    private UUID entityId;

    // Detalles
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Type(JsonType.class)
    @Column(name = "old_values", columnDefinition = "jsonb")
    private JsonNode oldValues; // Valores anteriores

    @Type(JsonType.class)
    @Column(name = "new_values", columnDefinition = "jsonb")
    private JsonNode newValues; // Valores nuevos

    @Size(max = 45)
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    // Métodos de utilidad
    public boolean isCreateAction() {
        return "CREATE".equalsIgnoreCase(this.action);
    }

    public boolean isUpdateAction() {
        return "UPDATE".equalsIgnoreCase(this.action);
    }

    public boolean isDeleteAction() {
        return "DELETE".equalsIgnoreCase(this.action);
    }

    public boolean isLoginAction() {
        return "LOGIN".equalsIgnoreCase(this.action);
    }

    public boolean isLogoutAction() {
        return "LOGOUT".equalsIgnoreCase(this.action);
    }

    public boolean hasOldValues() {
        return oldValues != null && !oldValues.isNull() && !oldValues.isEmpty();
    }

    public boolean hasNewValues() {
        return newValues != null && !newValues.isNull() && !newValues.isEmpty();
    }

    public boolean hasChanges() {
        return hasOldValues() || hasNewValues();
    }

    public String getUserFullName() {
        return user != null ? user.getFullName() : "Usuario Desconocido";
    }

    public String getUserEmail() {
        return user != null ? user.getEmail() : "N/A";
    }

    public String getActionDescription() {
        StringBuilder sb = new StringBuilder();

        if (user != null) {
            sb.append(user.getFullName());
        } else {
            sb.append("Usuario desconocido");
        }

        sb.append(" realizó ").append(action.toLowerCase());
        sb.append(" en ").append(entityType.toLowerCase());

        if (entityId != null) {
            sb.append(" (ID: ").append(entityId.toString().substring(0, 8)).append("...)");
        }

        return sb.toString();
    }

    public boolean isRecent() {
        if (createdAt != null) {
            return java.time.Duration.between(createdAt, LocalDateTime.now()).toHours() < 24;
        }
        return false;
    }

    public String getTimeSinceCreated() {
        if (createdAt == null) return "Desconocido";

        long minutes = java.time.Duration.between(createdAt, LocalDateTime.now()).toMinutes();

        if (minutes < 60) {
            return minutes + " minuto" + (minutes != 1 ? "s" : "");
        }

        long hours = minutes / 60;
        if (hours < 24) {
            return hours + " hora" + (hours != 1 ? "s" : "");
        }

        long days = hours / 24;
        return days + " día" + (days != 1 ? "s" : "");
    }

    // Métodos estáticos para crear logs
    public static AuditLog createLog(User user, String action, String entityType, UUID entityId, String description) {
        return AuditLog.builder()
                .user(user)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .description(description)
                .build();
    }

    public static AuditLog createLogWithChanges(User user, String action, String entityType, UUID entityId,
                                                String description, JsonNode oldValues, JsonNode newValues) {
        return AuditLog.builder()
                .user(user)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .description(description)
                .oldValues(oldValues)
                .newValues(newValues)
                .build();
    }
}