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
public class TenantResponse {

    private UUID id;
    private String fullName;
    private String phone;
    private String email;
    private Integer numberOfOccupants;
    private Boolean hasINE;
    private Boolean isActive;
    private Integer activeContractsCount;
    private LocalDateTime createdAt;
}