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
public class LocationDetailResponse {

    private UUID id;
    private UUID organizationId;
    private String organizationName;
    private String name;
    private String address;
    private String city;
    private String state;
    private String postalCode;
    private String description;
    private Integer totalProperties;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UUID createdBy;
}