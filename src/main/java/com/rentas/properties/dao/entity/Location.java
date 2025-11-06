package com.rentas.properties.dao.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "locations", indexes = {
        @Index(name = "idx_locations_name", columnList = "name"),
        @Index(name = "idx_locations_is_active", columnList = "is_active")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Location extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotBlank(message = "El nombre de la ubicación es obligatorio")
    @Size(max = 255)
    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @Size(max = 100)
    @Column(name = "city", length = 100)
    private String city;

    @Size(max = 100)
    @Column(name = "state", length = 100)
    private String state;

    @Size(max = 10)
    @Column(name = "postal_code", length = 10)
    private String postalCode;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    // Relación One-to-Many con Properties
    @OneToMany(mappedBy = "location", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Property> properties = new ArrayList<>();

    // Métodos de utilidad
    public void addProperty(Property property) {
        properties.add(property);
        property.setLocation(this);
    }

    public void removeProperty(Property property) {
        properties.remove(property);
        property.setLocation(null);
    }

    public int getTotalProperties() {
        return properties != null ? properties.size() : 0;
    }

    public long getAvailablePropertiesCount() {
        return properties != null
                ? properties.stream().filter(p -> "DISPONIBLE".equals(p.getStatus())).count()
                : 0;
    }

    public String getFullAddress() {
        StringBuilder sb = new StringBuilder();
        if (address != null) sb.append(address);
        if (city != null) sb.append(", ").append(city);
        if (state != null) sb.append(", ").append(state);
        if (postalCode != null) sb.append(" - ").append(postalCode);
        return sb.toString();
    }
}