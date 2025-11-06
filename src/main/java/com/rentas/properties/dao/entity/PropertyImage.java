package com.rentas.properties.dao.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad PropertyImage - Galería de imágenes de cada propiedad
 * Almacena URLs de Cloudinary
 */
@Entity
@Table(name = "property_images", indexes = {
        @Index(name = "idx_property_images_property", columnList = "property_id"),
        @Index(name = "idx_property_images_is_main", columnList = "is_main")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropertyImage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    // Relación Many-to-One con Property
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false, foreignKey = @ForeignKey(name = "fk_property_image_property"))
    private Property property;

    @NotBlank(message = "La URL de la imagen es obligatoria")
    @Column(name = "image_url", nullable = false, columnDefinition = "TEXT")
    private String imageUrl;

    @Size(max = 255)
    @Column(name = "image_public_id", length = 255)
    private String imagePublicId;

    @Size(max = 255)
    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "display_order")
    @Builder.Default
    private Integer displayOrder = 0;

    @Column(name = "is_main")
    @Builder.Default
    private Boolean isMain = false;

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
    public String getImageUrlThumbnail() {
        if (imageUrl != null && imageUrl.contains("cloudinary")) {
            // Genera URL de thumbnail de Cloudinary (ancho 300px)
            return imageUrl.replace("/upload/", "/upload/c_scale,w_300/");
        }
        return imageUrl;
    }

    public String getImageUrlMedium() {
        if (imageUrl != null && imageUrl.contains("cloudinary")) {
            // Genera URL de tamaño medio de Cloudinary (ancho 800px)
            return imageUrl.replace("/upload/", "/upload/c_scale,w_800/");
        }
        return imageUrl;
    }

    public boolean hasCloudinaryId() {
        return imagePublicId != null && !imagePublicId.isEmpty();
    }
}