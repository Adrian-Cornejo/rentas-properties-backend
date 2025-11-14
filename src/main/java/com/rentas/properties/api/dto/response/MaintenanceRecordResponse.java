package com.rentas.properties.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaintenanceRecordResponse {

    private UUID id;
    private UUID propertyId;
    private String propertyCode;
    private String propertyAddress;
    private UUID contractId;
    private String contractNumber;
    private String title;
    private String maintenanceType;
    private String category;
    private LocalDate maintenanceDate;
    private LocalDate completedDate;
    private BigDecimal estimatedCost;
    private BigDecimal actualCost;
    private String status;
    private String assignedTo;
    private Integer imageCount;
    private LocalDateTime createdAt;
}
