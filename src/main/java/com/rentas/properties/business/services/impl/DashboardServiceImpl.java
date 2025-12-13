package com.rentas.properties.business.services.impl;

import com.rentas.properties.api.dto.response.DashboardResponse;
import com.rentas.properties.api.exception.UnauthorizedAccessException;
import com.rentas.properties.business.services.DashboardService;
import com.rentas.properties.dao.repository.*;
import com.rentas.properties.dao.entity.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardServiceImpl implements DashboardService {

    private final PropertyRepository propertyRepository;
    private final TenantRepository tenantRepository;
    private final ContractRepository contractRepository;
    private final PaymentRepository paymentRepository;
    private final MaintenanceRecordRepository maintenanceRecordRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public DashboardResponse getDashboardData() {
        User currentUser = getCurrentUser();
        validateUserHasOrganization(currentUser);

        UUID organizationId = currentUser.getOrganization().getId();
        log.info("Obteniendo datos del dashboard para organización: {}", organizationId);

        return DashboardResponse.builder()
                .generalStats(buildGeneralStats(organizationId))
                .propertiesStats(buildPropertiesStats(organizationId))
                .contractsStats(buildContractsStats(organizationId))
                .paymentsStats(buildPaymentsStats(organizationId))
                .maintenanceStats(buildMaintenanceStats(organizationId))
                .recentActivity(buildRecentActivity(organizationId))
                .chartsData(buildChartsData(organizationId))
                .build();
    }

    private User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email;

        if (principal instanceof UserDetails) {
            email = ((UserDetails) principal).getUsername();
        } else {
            email = principal.toString();
        }

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedAccessException("Usuario no autenticado"));
    }

    private void validateUserHasOrganization(User user) {
        if (user.getOrganization() == null) {
            throw new UnauthorizedAccessException(
                    "Debes pertenecer a una organización para acceder al dashboard");
        }
    }

    private DashboardResponse.GeneralStats buildGeneralStats(UUID organizationId) {
        Long totalPropertiesLong = propertyRepository.countActiveByOrganization_Id(organizationId);
        Integer totalProperties = totalPropertiesLong != null ? totalPropertiesLong.intValue() : 0;

        Long totalTenantsLong = tenantRepository.countActiveByOrganization_Id(organizationId);
        Integer totalTenants = totalTenantsLong != null ? totalTenantsLong.intValue() : 0;

        Integer activeContracts = contractRepository.countByOrganization_IdAndStatus(organizationId, "ACTIVO");

        // Total de ocupantes (suma de occupantsCount de todos los inquilinos activos con contratos)
        Integer totalOccupants = tenantRepository.findByOrganization_IdAndIsActiveTrue(organizationId)
                .stream()
                .mapToInt(Tenant::getNumberOfOccupants)
                .sum();

        // Ingresos del mes actual
        LocalDate now = LocalDate.now();
        BigDecimal monthlyRevenue = paymentRepository.findByOrganizationIdAndStatus(organizationId, "PAGADO")
                .stream()
                .filter(p -> p.getPeriodYear() == now.getYear() && p.getPeriodMonth() == now.getMonthValue())
                .map(Payment::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Ingresos del año actual
        BigDecimal yearlyRevenue = paymentRepository.findByOrganizationIdAndStatus(organizationId, "PAGADO")
                .stream()
                .filter(p -> p.getPeriodYear() == now.getYear())
                .map(Payment::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return DashboardResponse.GeneralStats.builder()
                .totalProperties(totalProperties)
                .totalTenants(totalTenants)
                .activeContracts(activeContracts)
                .totalOccupants(totalOccupants)
                .monthlyRevenue(monthlyRevenue)
                .yearlyRevenue(yearlyRevenue)
                .build();
    }

    private DashboardResponse.PropertiesStats buildPropertiesStats(UUID organizationId) {
        List<Property> properties = propertyRepository.findByOrganization_IdAndIsActiveTrue(organizationId);

        Integer total = properties.size();
        Integer available = (int) properties.stream().filter(p -> "DISPONIBLE".equals(p.getStatus())).count();
        Integer rented = (int) properties.stream().filter(p -> "RENTADA".equals(p.getStatus())).count();
        Integer maintenance = (int) properties.stream().filter(p -> "MANTENIMIENTO".equals(p.getStatus())).count();

        Double occupancyRate = total > 0 ? (rented * 100.0) / total : 0.0;

        return DashboardResponse.PropertiesStats.builder()
                .total(total)
                .available(available)
                .rented(rented)
                .maintenance(maintenance)
                .occupancyRate(Math.round(occupancyRate * 100.0) / 100.0)
                .build();
    }

    private DashboardResponse.ContractsStats buildContractsStats(UUID organizationId) {
        List<Contract> contracts = contractRepository.findByOrganization_Id(organizationId);

        Integer total = contracts.size();
        Integer active = (int) contracts.stream().filter(c -> "ACTIVO".equals(c.getStatus())).count();
        Integer expired = (int) contracts.stream().filter(c -> "VENCIDO".equals(c.getStatus())).count();
        Integer renewed = (int) contracts.stream().filter(c -> "RENOVADO".equals(c.getStatus())).count();
        Integer canceled = (int) contracts.stream().filter(c -> "CANCELADO".equals(c.getStatus())).count();

        // Contratos que vencen en 30 días
        LocalDate thirtyDaysFromNow = LocalDate.now().plusDays(30);
        Integer expiringSoon = (int) contracts.stream()
                .filter(c -> "ACTIVO".equals(c.getStatus()))
                .filter(c -> c.getEndDate() != null &&
                        !c.getEndDate().isAfter(thirtyDaysFromNow) &&
                        c.getEndDate().isAfter(LocalDate.now()))
                .count();

        // Depósitos pendientes
        Integer pendingDepositsCount = (int) contracts.stream()
                .filter(c -> "PENDIENTE".equals(c.getDepositStatus()))
                .count();

        BigDecimal pendingDeposits = contracts.stream()
                .filter(c -> "PENDIENTE".equals(c.getDepositStatus()))
                .map(Contract::getDepositAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return DashboardResponse.ContractsStats.builder()
                .total(total)
                .active(active)
                .expired(expired)
                .expiringSoon(expiringSoon)
                .renewed(renewed)
                .canceled(canceled)
                .pendingDeposits(pendingDeposits)
                .pendingDepositsCount(pendingDepositsCount)
                .build();
    }

    private DashboardResponse.PaymentsStats buildPaymentsStats(UUID organizationId) {
        List<Payment> payments = paymentRepository.findByOrganizationId(organizationId);

        Integer total = payments.size();
        Integer pending = (int) payments.stream().filter(p -> "PENDIENTE".equals(p.getStatus())).count();
        Integer paid = (int) payments.stream().filter(p -> "PAGADO".equals(p.getStatus())).count();
        Integer overdue = (int) payments.stream().filter(p -> "ATRASADO".equals(p.getStatus())).count();

        BigDecimal totalAmount = payments.stream()
                .map(Payment::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal paidAmount = payments.stream()
                .filter(p -> "PAGADO".equals(p.getStatus()))
                .map(Payment::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal pendingAmount = payments.stream()
                .filter(p -> "PENDIENTE".equals(p.getStatus()))
                .map(Payment::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal overdueAmount = payments.stream()
                .filter(p -> "ATRASADO".equals(p.getStatus()))
                .map(Payment::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        LocalDate today = LocalDate.now();
        Integer dueToday = (int) payments.stream()
                .filter(p -> "PENDIENTE".equals(p.getStatus()))
                .filter(p -> p.getDueDate() != null && p.getDueDate().isEqual(today))
                .count();

        LocalDate oneWeekFromNow = today.plusDays(7);
        Integer dueThisWeek = (int) payments.stream()
                .filter(p -> "PENDIENTE".equals(p.getStatus()))
                .filter(p -> p.getDueDate() != null &&
                        !p.getDueDate().isBefore(today) &&
                        !p.getDueDate().isAfter(oneWeekFromNow))
                .count();

        return DashboardResponse.PaymentsStats.builder()
                .total(total)
                .pending(pending)
                .paid(paid)
                .overdue(overdue)
                .totalAmount(totalAmount)
                .paidAmount(paidAmount)
                .pendingAmount(pendingAmount)
                .overdueAmount(overdueAmount)
                .dueToday(dueToday)
                .dueThisWeek(dueThisWeek)
                .build();
    }

    private DashboardResponse.MaintenanceStats buildMaintenanceStats(UUID organizationId) {
        List<MaintenanceRecord> records = maintenanceRecordRepository.findByOrganization_Id(organizationId);

        Integer total = records.size();
        Integer pending = (int) records.stream().filter(r -> "PENDIENTE".equals(r.getStatus())).count();
        Integer inProgress = (int) records.stream().filter(r -> "EN_PROCESO".equals(r.getStatus())).count();
        Integer completed = (int) records.stream().filter(r -> "COMPLETADO".equals(r.getStatus())).count();

        BigDecimal estimatedCosts = records.stream()
                .map(MaintenanceRecord::getEstimatedCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal actualCosts = records.stream()
                .filter(r -> r.getActualCost() != null)
                .map(MaintenanceRecord::getActualCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return DashboardResponse.MaintenanceStats.builder()
                .total(total)
                .pending(pending)
                .inProgress(inProgress)
                .completed(completed)
                .estimatedCosts(estimatedCosts)
                .actualCosts(actualCosts)
                .build();
    }

    private DashboardResponse.RecentActivity buildRecentActivity(UUID organizationId) {
        return DashboardResponse.RecentActivity.builder()
                .propertyAlerts(buildPropertyAlerts(organizationId))
                .contractAlerts(buildContractAlerts(organizationId))
                .paymentAlerts(buildPaymentAlerts(organizationId))
                .topPropertiesByRevenue(buildTopPropertiesByRevenue(organizationId))
                .topPropertiesByOverduePayments(buildTopPropertiesByOverduePayments(organizationId))
                .build();
    }

    private List<DashboardResponse.PropertyAlert> buildPropertyAlerts(UUID organizationId) {
        List<DashboardResponse.PropertyAlert> alerts = new ArrayList<>();

        // Propiedades con mantenimiento pendiente
        List<MaintenanceRecord> pendingMaintenance = maintenanceRecordRepository
                .findByStatusAndOrganization_Id("PENDIENTE", organizationId);

        for (MaintenanceRecord record : pendingMaintenance) {
            Property property = record.getProperty();
            alerts.add(DashboardResponse.PropertyAlert.builder()
                    .propertyCode(property.getPropertyCode())
                    .address(property.getAddress())
                    .alertType("MAINTENANCE_REQUIRED")
                    .message("Mantenimiento pendiente: " + record.getTitle())
                    .daysCount(null)
                    .build());
        }

        return alerts.stream().limit(5).collect(Collectors.toList());
    }

    private List<DashboardResponse.ContractAlert> buildContractAlerts(UUID organizationId) {
        List<DashboardResponse.ContractAlert> alerts = new ArrayList<>();
        LocalDate now = LocalDate.now();
        LocalDate thirtyDaysFromNow = now.plusDays(30);

        // Contratos próximos a vencer
        List<Contract> expiringContracts = contractRepository.findByOrganization_IdAndStatus(organizationId, "ACTIVO")
                .stream()
                .filter(c -> c.getEndDate() != null &&
                        !c.getEndDate().isAfter(thirtyDaysFromNow) &&
                        c.getEndDate().isAfter(now))
                .collect(Collectors.toList());

        for (Contract contract : expiringContracts) {
            long daysUntilExpiry = java.time.temporal.ChronoUnit.DAYS.between(now, contract.getEndDate());
            alerts.add(DashboardResponse.ContractAlert.builder()
                    .contractNumber(contract.getContractNumber())
                    .propertyCode(contract.getProperty().getPropertyCode())
                    .propertyAddress(contract.getProperty().getAddress())
                    .alertType("EXPIRING_SOON")
                    .message("Vence en " + daysUntilExpiry + " días")
                    .daysUntilExpiry((int) daysUntilExpiry)
                    .build());
        }

        // Contratos con depósito pendiente
        List<Contract> pendingDeposits = contractRepository.findByOrganization_Id(organizationId)
                .stream()
                .filter(c -> "PENDIENTE".equals(c.getDepositStatus()))
                .collect(Collectors.toList());

        for (Contract contract : pendingDeposits) {
            alerts.add(DashboardResponse.ContractAlert.builder()
                    .contractNumber(contract.getContractNumber())
                    .propertyCode(contract.getProperty().getPropertyCode())
                    .propertyAddress(contract.getProperty().getAddress())
                    .alertType("DEPOSIT_PENDING")
                    .message("Depósito pendiente de $" + contract.getDepositAmount())
                    .daysUntilExpiry(null)
                    .build());
        }

        return alerts.stream()
                .sorted(Comparator.comparing(a -> a.getDaysUntilExpiry() != null ? a.getDaysUntilExpiry() : Integer.MAX_VALUE))
                .limit(10)
                .collect(Collectors.toList());
    }

    private List<DashboardResponse.PaymentAlert> buildPaymentAlerts(UUID organizationId) {
        List<DashboardResponse.PaymentAlert> alerts = new ArrayList<>();
        LocalDate today = LocalDate.now();
        LocalDate oneWeekFromNow = today.plusDays(7);

        List<Payment> payments = paymentRepository.findByOrganizationId(organizationId);

        // Pagos atrasados
        List<Payment> overduePayments = payments.stream()
                .filter(p -> "ATRASADO".equals(p.getStatus()))
                .sorted(Comparator.comparing(Payment::getDueDate))
                .collect(Collectors.toList());

        for (Payment payment : overduePayments) {
            long daysOverdue = java.time.temporal.ChronoUnit.DAYS.between(payment.getDueDate(), today);
            alerts.add(DashboardResponse.PaymentAlert.builder()
                    .contractNumber(payment.getContract().getContractNumber())
                    .propertyCode(payment.getContract().getProperty().getPropertyCode())
                    .propertyAddress(payment.getContract().getProperty().getAddress())
                    .alertType("OVERDUE")
                    .amount(payment.getTotalAmount())
                    .daysOverdue((int) daysOverdue)
                    .build());
        }

        // Pagos que vencen hoy
        List<Payment> dueToday = payments.stream()
                .filter(p -> "PENDIENTE".equals(p.getStatus()))
                .filter(p -> p.getDueDate() != null && p.getDueDate().isEqual(today))
                .collect(Collectors.toList());

        for (Payment payment : dueToday) {
            alerts.add(DashboardResponse.PaymentAlert.builder()
                    .contractNumber(payment.getContract().getContractNumber())
                    .propertyCode(payment.getContract().getProperty().getPropertyCode())
                    .propertyAddress(payment.getContract().getProperty().getAddress())
                    .alertType("DUE_TODAY")
                    .amount(payment.getTotalAmount())
                    .daysOverdue(0)
                    .build());
        }

        // Pagos que vencen esta semana
        List<Payment> dueThisWeek = payments.stream()
                .filter(p -> "PENDIENTE".equals(p.getStatus()))
                .filter(p -> p.getDueDate() != null &&
                        p.getDueDate().isAfter(today) &&
                        !p.getDueDate().isAfter(oneWeekFromNow))
                .collect(Collectors.toList());

        for (Payment payment : dueThisWeek) {
            alerts.add(DashboardResponse.PaymentAlert.builder()
                    .contractNumber(payment.getContract().getContractNumber())
                    .propertyCode(payment.getContract().getProperty().getPropertyCode())
                    .propertyAddress(payment.getContract().getProperty().getAddress())
                    .alertType("DUE_THIS_WEEK")
                    .amount(payment.getTotalAmount())
                    .daysOverdue(null)
                    .build());
        }

        return alerts.stream().limit(10).collect(Collectors.toList());
    }

    private List<DashboardResponse.TopProperty> buildTopPropertiesByRevenue(UUID organizationId) {
        List<Payment> paidPayments = paymentRepository.findByOrganizationIdAndStatus(organizationId, "PAGADO");

        Map<String, DashboardResponse.TopProperty> propertyRevenueMap = new HashMap<>();

        for (Payment payment : paidPayments) {
            String propertyCode = payment.getContract().getProperty().getPropertyCode();
            String address = payment.getContract().getProperty().getAddress();

            propertyRevenueMap.computeIfAbsent(propertyCode, k ->
                    DashboardResponse.TopProperty.builder()
                            .propertyCode(propertyCode)
                            .address(address)
                            .amount(BigDecimal.ZERO)
                            .count(0)
                            .build()
            );

            DashboardResponse.TopProperty current = propertyRevenueMap.get(propertyCode);
            current.setAmount(current.getAmount().add(payment.getTotalAmount()));
            current.setCount(current.getCount() + 1);
        }

        return propertyRevenueMap.values().stream()
                .sorted(Comparator.comparing(DashboardResponse.TopProperty::getAmount).reversed())
                .limit(5)
                .collect(Collectors.toList());
    }

    private List<DashboardResponse.TopProperty> buildTopPropertiesByOverduePayments(UUID organizationId) {
        List<Payment> overduePayments = paymentRepository.findByOrganizationIdAndStatus(organizationId, "ATRASADO");

        Map<String, DashboardResponse.TopProperty> propertyOverdueMap = new HashMap<>();

        for (Payment payment : overduePayments) {
            String propertyCode = payment.getContract().getProperty().getPropertyCode();
            String address = payment.getContract().getProperty().getAddress();

            propertyOverdueMap.computeIfAbsent(propertyCode, k ->
                    DashboardResponse.TopProperty.builder()
                            .propertyCode(propertyCode)
                            .address(address)
                            .amount(BigDecimal.ZERO)
                            .count(0)
                            .build()
            );

            DashboardResponse.TopProperty current = propertyOverdueMap.get(propertyCode);
            current.setAmount(current.getAmount().add(payment.getTotalAmount()));
            current.setCount(current.getCount() + 1);
        }

        return propertyOverdueMap.values().stream()
                .sorted(Comparator.comparing(DashboardResponse.TopProperty::getCount).reversed())
                .limit(5)
                .collect(Collectors.toList());
    }

    private DashboardResponse.ChartsData buildChartsData(UUID organizationId) {
        return DashboardResponse.ChartsData.builder()
                .monthlyRevenue(buildMonthlyRevenueChart(organizationId))
                .paymentStatus(buildPaymentStatusChart(organizationId))
                .propertyStatus(buildPropertyStatusChart(organizationId))
                .contractStatus(buildContractStatusChart(organizationId))
                .maintenanceTypes(buildMaintenanceTypeChart(organizationId))
                .build();
    }

    private DashboardResponse.MonthlyRevenueChart buildMonthlyRevenueChart(UUID organizationId) {
        LocalDate now = LocalDate.now();
        int currentYear = now.getYear();

        List<String> months = new ArrayList<>();
        List<BigDecimal> revenue = new ArrayList<>();
        List<Integer> paymentsCount = new ArrayList<>();

        List<Payment> paidPayments = paymentRepository.findByOrganizationIdAndStatus(organizationId, "PAGADO")
                .stream()
                .filter(p -> p.getPeriodYear() == currentYear)
                .collect(Collectors.toList());

        for (int month = 1; month <= 12; month++) {
            String monthName = LocalDate.of(currentYear, month, 1)
                    .getMonth()
                    .getDisplayName(TextStyle.SHORT, new Locale("es", "MX"));
            months.add(monthName);

            final int currentMonth = month;
            BigDecimal monthRevenue = paidPayments.stream()
                    .filter(p -> p.getPeriodMonth() == currentMonth)
                    .map(Payment::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            revenue.add(monthRevenue);

            int monthPaymentsCount = (int) paidPayments.stream()
                    .filter(p -> p.getPeriodMonth() == currentMonth)
                    .count();
            paymentsCount.add(monthPaymentsCount);
        }

        return DashboardResponse.MonthlyRevenueChart.builder()
                .months(months)
                .revenue(revenue)
                .paymentsCount(paymentsCount)
                .build();
    }

    private DashboardResponse.PaymentStatusChart buildPaymentStatusChart(UUID organizationId) {
        List<Payment> payments = paymentRepository.findByOrganizationId(organizationId);

        int paid = (int) payments.stream().filter(p -> "PAGADO".equals(p.getStatus())).count();
        int pending = (int) payments.stream().filter(p -> "PENDIENTE".equals(p.getStatus())).count();
        int overdue = (int) payments.stream().filter(p -> "ATRASADO".equals(p.getStatus())).count();

        return DashboardResponse.PaymentStatusChart.builder()
                .paid(paid)
                .pending(pending)
                .overdue(overdue)
                .build();
    }

    private DashboardResponse.PropertyStatusChart buildPropertyStatusChart(UUID organizationId) {
        List<Property> properties = propertyRepository.findByOrganization_IdAndIsActiveTrue(organizationId);

        int available = (int) properties.stream().filter(p -> "DISPONIBLE".equals(p.getStatus())).count();
        int rented = (int) properties.stream().filter(p -> "RENTADA".equals(p.getStatus())).count();
        int maintenance = (int) properties.stream().filter(p -> "MANTENIMIENTO".equals(p.getStatus())).count();

        return DashboardResponse.PropertyStatusChart.builder()
                .available(available)
                .rented(rented)
                .maintenance(maintenance)
                .build();
    }

    private DashboardResponse.ContractStatusChart buildContractStatusChart(UUID organizationId) {
        List<Contract> contracts = contractRepository.findByOrganization_Id(organizationId);

        int active = (int) contracts.stream().filter(c -> "ACTIVO".equals(c.getStatus())).count();
        int expired = (int) contracts.stream().filter(c -> "VENCIDO".equals(c.getStatus())).count();
        int renewed = (int) contracts.stream().filter(c -> "RENOVADO".equals(c.getStatus())).count();
        int canceled = (int) contracts.stream().filter(c -> "CANCELADO".equals(c.getStatus())).count();

        LocalDate thirtyDaysFromNow = LocalDate.now().plusDays(30);
        int expiringSoon = (int) contracts.stream()
                .filter(c -> "ACTIVO".equals(c.getStatus()))
                .filter(c -> c.getEndDate() != null &&
                        !c.getEndDate().isAfter(thirtyDaysFromNow) &&
                        c.getEndDate().isAfter(LocalDate.now()))
                .count();

        return DashboardResponse.ContractStatusChart.builder()
                .active(active)
                .expired(expired)
                .expiringSoon(expiringSoon)
                .renewed(renewed)
                .canceled(canceled)
                .build();
    }

    private DashboardResponse.MaintenanceTypeChart buildMaintenanceTypeChart(UUID organizationId) {
        List<MaintenanceRecord> records = maintenanceRecordRepository.findByOrganization_Id(organizationId);

        int preventivo = (int) records.stream().filter(r -> "PREVENTIVO".equals(r.getMaintenanceType())).count();
        int correctivo = (int) records.stream().filter(r -> "CORRECTIVO".equals(r.getMaintenanceType())).count();
        int emergencia = (int) records.stream().filter(r -> "EMERGENCIA".equals(r.getMaintenanceType())).count();

        return DashboardResponse.MaintenanceTypeChart.builder()
                .preventivo(preventivo)
                .correctivo(correctivo)
                .emergencia(emergencia)
                .build();
    }
}