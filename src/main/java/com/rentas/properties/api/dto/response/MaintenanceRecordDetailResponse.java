package com.rentas.properties.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaintenanceRecordDetailResponse {

    private UUID id;
    private UUID organizationId;
    private PropertyDto property;
    private ContractDto contract;
    private String title;
    private String description;
    private String maintenanceType;
    private String category;
    private LocalDate maintenanceDate;
    private LocalDate completedDate;
    private BigDecimal estimatedCost;
    private BigDecimal actualCost;
    private String status;
    private String assignedTo;
    private String notes;
    private List<ImageDto> images;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UUID createdBy;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PropertyDto {
        private UUID id;
        private String propertyCode;
        private String address;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContractDto {
        private UUID id;
        private String contractNumber;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImageDto {
        private UUID id;
        private String imageUrl;
        private String imagePublicId;
        private String imageType;
        private String description;
        private LocalDateTime createdAt;
    }
}
