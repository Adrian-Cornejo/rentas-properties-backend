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
public class ContractResponse {

    private UUID id;
    private UUID organizationId;
    private UUID propertyId;
    private String propertyCode;
    private String propertyAddress;
    private String contractNumber;
    private String tenantNames;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal monthlyRent;
    private BigDecimal waterFee;
    private BigDecimal depositAmount;
    private Boolean depositPaid;
    private String depositStatus;
    private String status;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
