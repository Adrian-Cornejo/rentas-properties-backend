package com.rentas.properties.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropertyResponse {

    private UUID id;
    private String propertyCode;
    private String propertyType;
    private String address;
    private BigDecimal monthlyRent;
    private BigDecimal waterFee;
    private String status;
    private UUID locationId;
    private String locationName;
    private Integer bedrooms;
    private Integer bathrooms;
    private Integer parkingSpaces;
    private BigDecimal totalAreaM2;
    private Boolean isActive;
    private LocalDateTime createdAt;
}