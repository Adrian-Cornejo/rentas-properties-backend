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
public class ContractSummaryResponse {

    private Long totalContracts;
    private Long activeContracts;
    private Long expiringSoonContracts;
    private Long pendingDepositsContracts;
    private BigDecimal monthlyProjectedIncome;
}
