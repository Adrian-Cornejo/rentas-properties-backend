package com.rentas.properties.dao.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entidad Property - Propiedades disponibles para renta
 * Incluye casas, departamentos y locales comerciales
 */
@Entity
@Table(name = "properties", indexes = {
        @Index(name = "idx_properties_location", columnList = "location_id"),
        @Index(name = "idx_properties_status", columnList = "status"),
        @Index(name = "idx_properties_type", columnList = "property_type"),
        @Index(name = "idx_properties_code", columnList = "property_code"),
        @Index(name = "idx_properties_slug", columnList = "public_url_slug")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Property extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    // Relación Many-to-One con Location
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", foreignKey = @ForeignKey(name = "fk_property_location"))
    private Location location;

    @NotBlank(message = "El código de propiedad es obligatorio")
    @Size(max = 50)
    @Column(name = "property_code", unique = true, nullable = false, length = 50)
    private String propertyCode;

    @NotBlank(message = "El tipo de propiedad es obligatorio")
    @Size(max = 50)
    @Column(name = "property_type", nullable = false, length = 50)
    private String propertyType; // CASA, DEPARTAMENTO, LOCAL_COMERCIAL

    @NotBlank(message = "La dirección es obligatoria")
    @Column(name = "address", nullable = false, columnDefinition = "TEXT")
    private String address;

    @NotNull(message = "La renta mensual es obligatoria")
    @DecimalMin(value = "0.0", inclusive = false, message = "La renta debe ser mayor a 0")
    @Column(name = "monthly_rent", nullable = false, precision = 10, scale = 2)
    private BigDecimal monthlyRent;

    @Column(name = "water_fee", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal waterFee = new BigDecimal("105.00");

    @Size(max = 50)
    @Column(name = "status", length = 50)
    @Builder.Default
    private String status = "DISPONIBLE"; // DISPONIBLE, RENTADA, MANTENIMIENTO

    // Características generales
    @Column(name = "floors")
    @Builder.Default
    private Integer floors = 1;

    @Column(name = "bedrooms")
    private Integer bedrooms;

    @Column(name = "bathrooms")
    private Integer bathrooms;

    @Column(name = "half_bathrooms")
    private Integer halfBathrooms;

    @Column(name = "has_living_room")
    @Builder.Default
    private Boolean hasLivingRoom = false;

    @Column(name = "has_dining_room")
    @Builder.Default
    private Boolean hasDiningRoom = false;

    @Column(name = "has_kitchen")
    @Builder.Default
    private Boolean hasKitchen = false;

    @Column(name = "has_service_area")
    @Builder.Default
    private Boolean hasServiceArea = false;

    @Column(name = "parking_spaces")
    @Builder.Default
    private Integer parkingSpaces = 0;

    @Column(name = "total_area_m2", precision = 8, scale = 2)
    private BigDecimal totalAreaM2;

    // Servicios incluidos
    @Column(name = "includes_water")
    @Builder.Default
    private Boolean includesWater = false;

    @Column(name = "includes_electricity")
    @Builder.Default
    private Boolean includesElectricity = false;

    @Column(name = "includes_gas")
    @Builder.Default
    private Boolean includesGas = false;

    @Column(name = "includes_internet")
    @Builder.Default
    private Boolean includesInternet = false;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Size(max = 255)
    @Column(name = "public_url_slug", unique = true, length = 255)
    private String publicUrlSlug;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    // Relaciones
    @OneToMany(mappedBy = "property", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<PropertyImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "property", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Contract> contracts = new ArrayList<>();

    @OneToMany(mappedBy = "property", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<MaintenanceRecord> maintenanceRecords = new ArrayList<>();

    // Métodos de utilidad
    public void addImage(PropertyImage image) {
        images.add(image);
        image.setProperty(this);
    }

    public void removeImage(PropertyImage image) {
        images.remove(image);
        image.setProperty(null);
    }

    public PropertyImage getMainImage() {
        return images.stream()
                .filter(PropertyImage::getIsMain)
                .findFirst()
                .orElse(null);
    }

    public boolean isAvailable() {
        return "DISPONIBLE".equalsIgnoreCase(this.status);
    }

    public boolean isRented() {
        return "RENTADA".equalsIgnoreCase(this.status);
    }

    public boolean isUnderMaintenance() {
        return "MANTENIMIENTO".equalsIgnoreCase(this.status);
    }

    public BigDecimal getTotalMonthlyPayment() {
        return monthlyRent.add(waterFee != null ? waterFee : BigDecimal.ZERO);
    }

    public String getFullAddress() {
        StringBuilder sb = new StringBuilder(address);
        if (location != null) {
            sb.append(", ").append(location.getName());
            if (location.getCity() != null) {
                sb.append(", ").append(location.getCity());
            }
        }
        return sb.toString();
    }

    public int getTotalImages() {
        return images != null ? images.size() : 0;
    }

    public Contract getActiveContract() {
        return contracts.stream()
                .filter(c -> "ACTIVO".equals(c.getStatus()))
                .findFirst()
                .orElse(null);
    }
}