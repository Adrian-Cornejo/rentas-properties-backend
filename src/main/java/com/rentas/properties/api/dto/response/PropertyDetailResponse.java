package com.rentas.properties.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropertyDetailResponse {

    private UUID id;
    private UUID organizationId;
    private String organizationName;
    private LocationDto location;
    private String propertyCode;
    private String propertyType;
    private String address;
    private BigDecimal monthlyRent;
    private BigDecimal waterFee;
    private String status;
    private Integer floors;
    private Integer bedrooms;
    private Integer bathrooms;
    private Integer halfBathrooms;
    private Boolean hasLivingRoom;
    private Boolean hasDiningRoom;
    private Boolean hasKitchen;
    private Boolean hasServiceArea;
    private Integer parkingSpaces;
    private BigDecimal totalAreaM2;
    private Boolean includesWater;
    private Boolean includesElectricity;
    private Boolean includesGas;
    private Boolean includesInternet;
    private String notes;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UUID createdBy;
    private List<String> imageUrls;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LocationDto {
        private UUID id;
        private String name;
        private String city;
        private String state;
    }
}