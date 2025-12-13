package com.rentas.properties.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {

    // General Stats
    private GeneralStats generalStats;

    // Properties Stats
    private PropertiesStats propertiesStats;

    // Contracts Stats
    private ContractsStats contractsStats;

    // Payments Stats
    private PaymentsStats paymentsStats;

    // Maintenance Stats
    private MaintenanceStats maintenanceStats;

    // Recent Activity
    private RecentActivity recentActivity;

    // Charts Data
    private ChartsData chartsData;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GeneralStats {
        private Integer totalProperties;
        private Integer totalTenants;
        private Integer activeContracts;
        private Integer totalOccupants;
        private BigDecimal monthlyRevenue;
        private BigDecimal yearlyRevenue;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PropertiesStats {
        private Integer total;
        private Integer available;
        private Integer rented;
        private Integer maintenance;
        private Double occupancyRate; // Porcentaje
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContractsStats {
        private Integer total;
        private Integer active;
        private Integer expired;
        private Integer expiringSoon; // 30 días
        private Integer renewed;
        private Integer canceled;
        private BigDecimal pendingDeposits;
        private Integer pendingDepositsCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentsStats {
        private Integer total;
        private Integer pending;
        private Integer paid;
        private Integer overdue;
        private BigDecimal totalAmount;
        private BigDecimal paidAmount;
        private BigDecimal pendingAmount;
        private BigDecimal overdueAmount;
        private Integer dueToday;
        private Integer dueThisWeek;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MaintenanceStats {
        private Integer total;
        private Integer pending;
        private Integer inProgress;
        private Integer completed;
        private BigDecimal estimatedCosts;
        private BigDecimal actualCosts;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentActivity {
        private List<PropertyAlert> propertyAlerts;
        private List<ContractAlert> contractAlerts;
        private List<PaymentAlert> paymentAlerts;
        private List<TopProperty> topPropertiesByRevenue;
        private List<TopProperty> topPropertiesByOverduePayments;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PropertyAlert {
        private String propertyCode;
        private String address;
        private String alertType; // MAINTENANCE_REQUIRED, VACANT_LONG_TIME
        private String message;
        private Integer daysCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContractAlert {
        private String contractNumber;
        private String propertyCode;
        private String propertyAddress;
        private String alertType; // EXPIRING_SOON, DEPOSIT_PENDING
        private String message;
        private Integer daysUntilExpiry;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentAlert {
        private String contractNumber;
        private String propertyCode;
        private String propertyAddress;
        private String alertType; // OVERDUE, DUE_TODAY, DUE_THIS_WEEK
        private BigDecimal amount;
        private Integer daysOverdue;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopProperty {
        private String propertyCode;
        private String address;
        private BigDecimal amount;
        private Integer count;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChartsData {
        private MonthlyRevenueChart monthlyRevenue;
        private PaymentStatusChart paymentStatus;
        private PropertyStatusChart propertyStatus;
        private ContractStatusChart contractStatus;
        private MaintenanceTypeChart maintenanceTypes;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyRevenueChart {
        private List<String> months; // ["Ene", "Feb", "Mar", ...]
        private List<BigDecimal> revenue; // [12000, 15000, 13500, ...]
        private List<Integer> paymentsCount; // [10, 12, 11, ...]
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentStatusChart {
        private Integer paid;
        private Integer pending;
        private Integer overdue;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PropertyStatusChart {
        private Integer available;
        private Integer rented;
        private Integer maintenance;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContractStatusChart {
        private Integer active;
        private Integer expired;
        private Integer expiringSoon;
        private Integer renewed;
        private Integer canceled;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MaintenanceTypeChart {
        private Integer preventivo;
        private Integer correctivo;
        private Integer emergencia;
    }
}