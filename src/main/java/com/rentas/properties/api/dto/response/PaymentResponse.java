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
public class PaymentResponse {

    private UUID id;
    private UUID contractId;
    private String contractNumber;
    private String propertyCode;
    private String propertyAddress;
    private String paymentType;
    private LocalDate dueDate;
    private Integer periodMonth;
    private Integer periodYear;
    private BigDecimal amount;
    private BigDecimal lateFee;
    private BigDecimal totalAmount;
    private String status;
    private String paymentMethod;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
}
