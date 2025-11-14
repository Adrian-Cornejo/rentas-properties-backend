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
public class MaintenanceRecordSummaryResponse {

    private Long totalRecords;
    private Long pendingRecords;
    private Long completedRecords;
    private BigDecimal totalEstimatedCost;
    private BigDecimal totalActualCost;
    private Long preventiveCount;
    private Long correctiveCount;
    private Long emergencyCount;
}
