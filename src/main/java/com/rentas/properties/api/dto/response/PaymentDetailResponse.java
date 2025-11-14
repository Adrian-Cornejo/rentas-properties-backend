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
public class PaymentDetailResponse {

    private UUID id;
    private ContractDto contract;
    private String paymentType;
    private LocalDate paymentDate;
    private LocalDate dueDate;
    private Integer periodMonth;
    private Integer periodYear;
    private BigDecimal amount;
    private BigDecimal lateFee;
    private BigDecimal totalAmount;
    private String status;
    private String paymentMethod;
    private String referenceNumber;
    private String notes;
    private LocalDateTime paidAt;
    private UUID collectedBy;
    private String collectedByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContractDto {
        private UUID id;
        private String contractNumber;
        private String propertyCode;
        private String propertyAddress;
    }
}
