package com.rentas.properties.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentSummaryResponse {

    private Long totalPayments;
    private Long pendingPayments;
    private Long overduePayments;
    private Long paidPayments;
    private BigDecimal totalPendingAmount;
    private BigDecimal totalCollectedAmount;
    private BigDecimal totalLateFees;
    private BigDecimal currentMonthIncome;
}
