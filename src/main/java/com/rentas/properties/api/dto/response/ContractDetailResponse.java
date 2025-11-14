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
public class ContractDetailResponse {

    private UUID id;
    private UUID organizationId;
    private String organizationName;
    private PropertyDto property;
    private List<TenantDto> tenants;
    private String contractNumber;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate signedDate;
    private BigDecimal monthlyRent;
    private BigDecimal waterFee;
    private BigDecimal advancePayment;
    private BigDecimal depositAmount;
    private Boolean depositPaid;
    private LocalDate depositPaymentDeadline;
    private String depositStatus;
    private BigDecimal depositReturnAmount;
    private LocalDate depositReturnDate;
    private String depositDeductionReason;
    private String status;
    private String contractDocumentUrl;
    private String contractDocumentPublicId;
    private String notes;
    private Boolean isActive;
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
        private String propertyType;
        private String status;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TenantDto {
        private UUID id;
        private String fullName;
        private String phone;
        private String email;
        private Boolean isPrimary;
        private String relationship;
    }
}
