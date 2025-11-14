package com.rentas.properties.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantDetailResponse {

    private UUID id;
    private UUID organizationId;
    private String organizationName;
    private String fullName;
    private String phone;
    private String email;
    private String ineNumber;
    private String ineImageUrl;
    private String inePublicId;
    private Integer numberOfOccupants;
    private String notes;
    private Boolean isActive;
    private Integer activeContractsCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UUID createdBy;
}