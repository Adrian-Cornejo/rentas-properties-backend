package com.rentas.properties.dao.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad MaintenanceImage - Evidencias fotográficas de mantenimiento
 * Almacena fotos de antes, después y evidencias
 */
@Entity
@Table(name = "maintenance_images", indexes = {
        @Index(name = "idx_maintenance_images_maintenance", columnList = "maintenance_id")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaintenanceImage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    // Relación Many-to-One con MaintenanceRecord
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "maintenance_id", nullable = false, foreignKey = @ForeignKey(name = "fk_maintenance_image_maintenance"))
    private MaintenanceRecord maintenanceRecord;

    @NotBlank(message = "La URL de la imagen es obligatoria")
    @Column(name = "image_url", nullable = false, columnDefinition = "TEXT")
    private String imageUrl;

    @Size(max = 255)
    @Column(name = "image_public_id", length = 255)
    private String imagePublicId;

    @Size(max = 255)
    @Column(name = "description", length = 255)
    private String description;

    @Size(max = 50)
    @Column(name = "image_type", length = 50)
    @Builder.Default
    private String imageType = "EVIDENCIA"; // ANTES, DESPUES, EVIDENCIA

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
    public boolean isBeforeImage() {
        return "ANTES".equalsIgnoreCase(this.imageType);
    }

    public boolean isAfterImage() {
        return "DESPUES".equalsIgnoreCase(this.imageType);
    }

    public boolean isEvidenceImage() {
        return "EVIDENCIA".equalsIgnoreCase(this.imageType);
    }

    public String getImageUrlThumbnail() {
        if (imageUrl != null && imageUrl.contains("cloudinary")) {
            return imageUrl.replace("/upload/", "/upload/c_scale,w_300/");
        }
        return imageUrl;
    }

    public String getImageUrlMedium() {
        if (imageUrl != null && imageUrl.contains("cloudinary")) {
            return imageUrl.replace("/upload/", "/upload/c_scale,w_800/");
        }
        return imageUrl;
    }

    public boolean hasCloudinaryId() {
        return imagePublicId != null && !imagePublicId.isEmpty();
    }
}