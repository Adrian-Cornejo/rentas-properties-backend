package com.rentas.properties.dao.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad Document - Documentos generales del sistema
 * Usa relaciones polimórficas para asociar a diferentes entidades
 */
@Entity
@Table(name = "documents", indexes = {
        @Index(name = "idx_documents_entity", columnList = "entity_type, entity_id"),
        @Index(name = "idx_documents_type", columnList = "document_type")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    // Relaciones polimórficas
    @NotBlank(message = "El tipo de entidad es obligatorio")
    @Size(max = 50)
    @Column(name = "entity_type", nullable = false, length = 50)
    private String entityType; // PROPERTY, CONTRACT, TENANT, MAINTENANCE

    @Column(name = "entity_id", nullable = false)
    private UUID entityId;

    // Detalles del documento
    @NotBlank(message = "El tipo de documento es obligatorio")
    @Size(max = 50)
    @Column(name = "document_type", nullable = false, length = 50)
    private String documentType; // INE, CONTRATO, COMPROBANTE, EVIDENCIA

    @NotBlank(message = "El nombre del archivo es obligatorio")
    @Size(max = 255)
    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @NotBlank(message = "La URL del archivo es obligatoria")
    @Column(name = "file_url", nullable = false, columnDefinition = "TEXT")
    private String fileUrl;

    @Size(max = 255)
    @Column(name = "file_public_id", length = 255)
    private String filePublicId;

    @Column(name = "file_size")
    private Integer fileSize; // En bytes

    @Size(max = 100)
    @Column(name = "mime_type", length = 100)
    private String mimeType;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

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
    public boolean isPropertyDocument() {
        return "PROPERTY".equalsIgnoreCase(this.entityType);
    }

    public boolean isContractDocument() {
        return "CONTRACT".equalsIgnoreCase(this.entityType);
    }

    public boolean isTenantDocument() {
        return "TENANT".equalsIgnoreCase(this.entityType);
    }

    public boolean isMaintenanceDocument() {
        return "MAINTENANCE".equalsIgnoreCase(this.entityType);
    }

    public boolean isINE() {
        return "INE".equalsIgnoreCase(this.documentType);
    }

    public boolean isContract() {
        return "CONTRATO".equalsIgnoreCase(this.documentType);
    }

    public boolean isReceipt() {
        return "COMPROBANTE".equalsIgnoreCase(this.documentType);
    }

    public boolean isEvidence() {
        return "EVIDENCIA".equalsIgnoreCase(this.documentType);
    }

    public boolean isPDF() {
        return mimeType != null && mimeType.contains("pdf");
    }

    public boolean isImage() {
        return mimeType != null && mimeType.startsWith("image/");
    }

    public boolean hasCloudinaryId() {
        return filePublicId != null && !filePublicId.isEmpty();
    }

    public String getFileSizeFormatted() {
        if (fileSize == null) return "Desconocido";

        double kb = fileSize / 1024.0;
        if (kb < 1024) {
            return String.format("%.2f KB", kb);
        }

        double mb = kb / 1024.0;
        return String.format("%.2f MB", mb);
    }

    public String getFileExtension() {
        if (fileName != null && fileName.contains(".")) {
            return fileName.substring(fileName.lastIndexOf(".") + 1).toUpperCase();
        }
        return "N/A";
    }

    public void deactivate() {
        this.isActive = false;
    }

    public void activate() {
        this.isActive = true;
    }
}