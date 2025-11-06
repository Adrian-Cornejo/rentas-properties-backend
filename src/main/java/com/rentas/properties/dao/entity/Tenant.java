package com.rentas.properties.dao.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entidad Tenant - Arrendatarios/Inquilinos
 * Almacena información de las personas que rentan propiedades
 */
@Entity
@Table(name = "tenants", indexes = {
        @Index(name = "idx_tenants_phone", columnList = "phone"),
        @Index(name = "idx_tenants_name", columnList = "full_name"),
        @Index(name = "idx_tenants_is_active", columnList = "is_active")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Tenant extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotBlank(message = "El nombre completo es obligatorio")
    @Size(max = 255)
    @Column(name = "full_name", nullable = false, length = 255)
    private String fullName;

    @NotBlank(message = "El teléfono es obligatorio")
    @Size(max = 20)
    @Column(name = "phone", nullable = false, length = 20)
    private String phone;

    @Email(message = "Email debe ser válido")
    @Size(max = 255)
    @Column(name = "email", length = 255)
    private String email;

    @Size(max = 50)
    @Column(name = "ine_number", length = 50)
    private String ineNumber;

    @Column(name = "ine_image_url", columnDefinition = "TEXT")
    private String ineImageUrl;

    @Size(max = 255)
    @Column(name = "ine_public_id", length = 255)
    private String inePublicId;

    @Column(name = "number_of_occupants")
    @Builder.Default
    private Integer numberOfOccupants = 1;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    // Relación Many-to-Many con Contracts a través de ContractTenant
    @OneToMany(mappedBy = "tenant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ContractTenant> contractTenants = new ArrayList<>();

    // Métodos de utilidad
    public boolean hasINE() {
        return ineNumber != null && !ineNumber.isEmpty();
    }

    public boolean hasINEImage() {
        return ineImageUrl != null && !ineImageUrl.isEmpty();
    }

    public int getActiveContractsCount() {
        return contractTenants != null
                ? (int) contractTenants.stream()
                .filter(ct -> "ACTIVO".equals(ct.getContract().getStatus()))
                .count()
                : 0;
    }

    public String getContactInfo() {
        StringBuilder sb = new StringBuilder(phone);
        if (email != null && !email.isEmpty()) {
            sb.append(" | ").append(email);
        }
        return sb.toString();
    }
}