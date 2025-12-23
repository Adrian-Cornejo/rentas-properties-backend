package com.rentas.properties.business.services.impl;

import com.rentas.properties.api.dto.request.*;
import com.rentas.properties.api.dto.response.*;
import com.rentas.properties.api.exception.ReportDateRangeException;
import com.rentas.properties.api.exception.ReportFeatureNotAvailableException;
import com.rentas.properties.api.exception.UnauthorizedAccessException;
import com.rentas.properties.business.services.ReportService;
import com.rentas.properties.dao.entity.*;
import com.rentas.properties.dao.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportServiceImpl implements ReportService {

    private final PaymentRepository paymentRepository;
    private final PropertyRepository propertyRepository;
    private final ContractRepository contractRepository;
    private final MaintenanceRecordRepository maintenanceRecordRepository;
    private final OrganizationRepository organizationRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public FinancialReportResponse generateFinancialReport(FinancialReportRequest request) {
        log.info("Generando reporte financiero para rango: {} - {}", request.getStartDate(), request.getEndDate());

        UUID organizationId = getCurrentUser().getOrganization().getId();
        validateDateRangeForPlan(request.getStartDate(), request.getEndDate(), organizationId);

        List<Payment> payments = getFilteredPayments(request, organizationId);
        List<MaintenanceRecord> maintenances = getMaintenancesForDateRange(
                request.getStartDate(), request.getEndDate(), organizationId, request.getPropertyId());

        return FinancialReportResponse.builder()
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .summary(buildFinancialSummary(payments, maintenances))
                .monthlyIncomes(buildMonthlyIncomes(payments, maintenances, request.getStartDate(), request.getEndDate()))
                .propertyIncomes(buildPropertyIncomes(payments, maintenances, organizationId, request))
                .comparison(buildPeriodComparison(request, organizationId))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public OccupancyReportResponse generateOccupancyReport(OccupancyReportRequest request) {
        log.info("Generando reporte de ocupación para rango: {} - {}", request.getStartDate(), request.getEndDate());

        UUID organizationId = getCurrentUser().getOrganization().getId();
        validateDateRangeForPlan(request.getStartDate(), request.getEndDate(), organizationId);

        List<Property> properties = getFilteredProperties(request, organizationId);
        List<Contract> contracts = contractRepository.findByOrganization_Id(organizationId);

        return OccupancyReportResponse.builder()
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .summary(buildOccupancySummary(properties, contracts, request.getStartDate(), request.getEndDate()))
                .monthlyOccupancy(buildMonthlyOccupancy(properties, contracts, request.getStartDate(), request.getEndDate()))
                .propertyOccupancy(buildPropertyOccupancy(properties, contracts, request.getStartDate(), request.getEndDate()))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentReportResponse generatePaymentReport(PaymentReportRequest request) {
        log.info("Generando reporte de pagos para rango: {} - {}", request.getStartDate(), request.getEndDate());

        UUID organizationId = getCurrentUser().getOrganization().getId();
        validateDateRangeForPlan(request.getStartDate(), request.getEndDate(), organizationId);

        List<Payment> payments = getFilteredPaymentsForReport(request, organizationId);

        return PaymentReportResponse.builder()
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .summary(buildPaymentSummary(payments))
                .paymentDetails(buildPaymentDetails(payments))
                .topDelinquents(buildTopDelinquents(payments))
                .monthlyDelinquency(buildMonthlyDelinquency(payments, request.getStartDate(), request.getEndDate()))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public MaintenanceReportResponse generateMaintenanceReport(MaintenanceReportRequest request) {
        log.info("Generando reporte de mantenimientos para rango: {} - {}", request.getStartDate(), request.getEndDate());

        UUID organizationId = getCurrentUser().getOrganization().getId();
        validateDateRangeForPlan(request.getStartDate(), request.getEndDate(), organizationId);

        List<MaintenanceRecord> maintenances = getFilteredMaintenances(request, organizationId);

        return MaintenanceReportResponse.builder()
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .summary(buildMaintenanceSummary(maintenances))
                .typeBreakdown(buildTypeBreakdown(maintenances))
                .categoryBreakdown(buildCategoryBreakdown(maintenances))
                .propertyMaintenances(buildPropertyMaintenances(maintenances, organizationId))
                .costComparison(buildCostComparison(maintenances))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ExecutiveReportResponse generateExecutiveReport(ExecutiveReportRequest request) {
        log.info("Generando reporte ejecutivo para año: {}", request.getYear());

        UUID organizationId = getCurrentUser().getOrganization().getId();

        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new IllegalStateException("Organización no encontrada"));

        SubscriptionPlan plan = organization.getSubscriptionPlan();
        if (plan == null || !plan.getHasAdvancedReports()) {
            throw new ReportFeatureNotAvailableException(
                    "Tu plan no incluye reportes avanzados. Actualiza a plan PROFESIONAL o EMPRESARIAL.");
        }

        LocalDate[] period = calculatePeriodDates(request.getYear(), request.getPeriod());
        LocalDate startDate = period[0];
        LocalDate endDate = period[1];

        Integer comparisonYear = request.getComparisonYear() != null ? request.getComparisonYear() : request.getYear() - 1;
        LocalDate[] comparisonPeriod = calculatePeriodDates(comparisonYear, request.getPeriod());

        return ExecutiveReportResponse.builder()
                .year(request.getYear())
                .comparisonYear(comparisonYear)
                .period(request.getPeriod())
                .kpis(buildExecutiveKPIs(organizationId, startDate, endDate))
                .yearComparison(buildYearComparison(organizationId, startDate, endDate, comparisonPeriod[0], comparisonPeriod[1]))
                .propertyROIs(buildPropertyROIs(organizationId, startDate, endDate, request.getLocationId()))
                .locationProfitability(buildLocationProfitability(organizationId, startDate, endDate))
                .propertyPerformance(buildPropertyPerformance(organizationId, startDate, endDate))
                .build();
    }

    @Override
    public void validateDateRangeForPlan(LocalDate startDate, LocalDate endDate, UUID organizationId) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new IllegalStateException("Organización no encontrada"));

        SubscriptionPlan plan = organization.getSubscriptionPlan();
        if (plan == null) {
            throw new IllegalStateException("La organización no tiene un plan de suscripción asignado");
        }

        Integer reportHistoryDays = plan.getReportHistoryDays();

        if (reportHistoryDays == -1) {
            log.info("Plan EMPRESARIAL: sin restricción de fechas");
            return;
        }

        LocalDate earliestAllowedDate = LocalDate.now().minusDays(reportHistoryDays);

        if (startDate.isBefore(earliestAllowedDate)) {
            throw new ReportDateRangeException(
                    String.format("La fecha de inicio (%s) está fuera del límite de tu plan. " +
                                    "Tu plan permite consultar hasta %d días atrás (desde %s). " +
                                    "Actualiza tu plan para acceder a más historial.",
                            startDate, reportHistoryDays, earliestAllowedDate)
            );
        }

        if (endDate.isBefore(startDate)) {
            throw new ReportDateRangeException("La fecha de fin no puede ser anterior a la fecha de inicio");
        }

        if (endDate.isAfter(LocalDate.now())) {
            throw new ReportDateRangeException("La fecha de fin no puede ser futura");
        }

        log.info("Validación de rango de fechas exitosa para plan: {}, días permitidos: {}",
                plan.getPlanName(), reportHistoryDays);
    }

    @Override
    public byte[] exportReportToPdf(String reportType, Object reportData) {
        UUID organizationId = getCurrentUser().getOrganization().getId();
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new IllegalStateException("Organización no encontrada"));

        SubscriptionPlan plan = organization.getSubscriptionPlan();
        if (plan == null || !plan.getHasPdfReports()) {
            throw new ReportFeatureNotAvailableException(
                    "Tu plan no incluye exportación a PDF. Actualiza a plan BÁSICO, PROFESIONAL o EMPRESARIAL.");
        }

        log.info("Exportando reporte {} a PDF", reportType);
        throw new UnsupportedOperationException("Exportación a PDF en desarrollo");
    }

    @Override
    public byte[] exportReportToExcel(String reportType, Object reportData) {
        UUID organizationId = getCurrentUser().getOrganization().getId();
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new IllegalStateException("Organización no encontrada"));

        SubscriptionPlan plan = organization.getSubscriptionPlan();
        if (plan == null || !plan.getHasDataExport()) {
            throw new ReportFeatureNotAvailableException(
                    "Tu plan no incluye exportación de datos. Actualiza a plan PROFESIONAL o EMPRESARIAL.");
        }

        log.info("Exportando reporte {} a Excel", reportType);
        throw new UnsupportedOperationException("Exportación a Excel en desarrollo");
    }

    // ========================================
    // MÉTODOS AUXILIARES - FILTRADO DE DATOS
    // ========================================

    private List<Payment> getFilteredPayments(FinancialReportRequest request, UUID organizationId) {
        List<Payment> payments = paymentRepository.findByOrganizationId(organizationId).stream()
                .filter(p -> !p.getDueDate().isBefore(request.getStartDate()) &&
                        !p.getDueDate().isAfter(request.getEndDate()))
                .filter(p -> "PAGADO".equals(p.getStatus()))
                .collect(Collectors.toList());

        if (request.getPropertyId() != null) {
            payments = payments.stream()
                    .filter(p -> p.getContract().getProperty().getId().equals(request.getPropertyId()))
                    .collect(Collectors.toList());
        }

        if (request.getLocationId() != null) {
            payments = payments.stream()
                    .filter(p -> p.getContract().getProperty().getLocation() != null &&
                            p.getContract().getProperty().getLocation().getId().equals(request.getLocationId()))
                    .collect(Collectors.toList());
        }

        if (request.getPropertyType() != null) {
            payments = payments.stream()
                    .filter(p -> request.getPropertyType().equals(p.getContract().getProperty().getPropertyType()))
                    .collect(Collectors.toList());
        }

        return payments;
    }

    private List<MaintenanceRecord> getMaintenancesForDateRange(LocalDate startDate, LocalDate endDate,
                                                                UUID organizationId, UUID propertyId) {
        return maintenanceRecordRepository.findByOrganization_Id(organizationId).stream()
                .filter(m -> !m.getMaintenanceDate().isBefore(startDate) &&
                        !m.getMaintenanceDate().isAfter(endDate))
                .filter(m -> "COMPLETADO".equals(m.getStatus()))
                .filter(m -> propertyId == null || m.getProperty().getId().equals(propertyId))
                .collect(Collectors.toList());
    }

    private List<Property> getFilteredProperties(OccupancyReportRequest request, UUID organizationId) {
        List<Property> properties = propertyRepository.findByOrganization_IdAndIsActiveTrue(organizationId);

        if (request.getLocationId() != null) {
            properties = properties.stream()
                    .filter(p -> p.getLocation() != null && p.getLocation().getId().equals(request.getLocationId()))
                    .collect(Collectors.toList());
        }

        if (request.getPropertyType() != null) {
            properties = properties.stream()
                    .filter(p -> request.getPropertyType().equals(p.getPropertyType()))
                    .collect(Collectors.toList());
        }

        return properties;
    }

    private List<Payment> getFilteredPaymentsForReport(PaymentReportRequest request, UUID organizationId) {
        List<Payment> payments = paymentRepository.findByOrganizationId(organizationId).stream()
                .filter(p -> !p.getDueDate().isBefore(request.getStartDate()) &&
                        !p.getDueDate().isAfter(request.getEndDate()))
                .collect(Collectors.toList());

        if (request.getPaymentStatus() != null) {
            payments = payments.stream()
                    .filter(p -> request.getPaymentStatus().equals(p.getStatus()))
                    .collect(Collectors.toList());
        }

        if (request.getPropertyId() != null) {
            payments = payments.stream()
                    .filter(p -> p.getContract().getProperty().getId().equals(request.getPropertyId()))
                    .collect(Collectors.toList());
        }

        if (request.getContractId() != null) {
            payments = payments.stream()
                    .filter(p -> p.getContract().getId().equals(request.getContractId()))
                    .collect(Collectors.toList());
        }

        return payments;
    }

    private List<MaintenanceRecord> getFilteredMaintenances(MaintenanceReportRequest request, UUID organizationId) {
        List<MaintenanceRecord> maintenances = maintenanceRecordRepository.findByOrganization_Id(organizationId).stream()
                .filter(m -> !m.getMaintenanceDate().isBefore(request.getStartDate()) &&
                        !m.getMaintenanceDate().isAfter(request.getEndDate()))
                .collect(Collectors.toList());

        if (request.getMaintenanceType() != null) {
            maintenances = maintenances.stream()
                    .filter(m -> request.getMaintenanceType().equals(m.getMaintenanceType()))
                    .collect(Collectors.toList());
        }

        if (request.getStatus() != null) {
            maintenances = maintenances.stream()
                    .filter(m -> request.getStatus().equals(m.getStatus()))
                    .collect(Collectors.toList());
        }

        if (request.getPropertyId() != null) {
            maintenances = maintenances.stream()
                    .filter(m -> m.getProperty().getId().equals(request.getPropertyId()))
                    .collect(Collectors.toList());
        }

        if (request.getCategory() != null) {
            maintenances = maintenances.stream()
                    .filter(m -> request.getCategory().equals(m.getCategory()))
                    .collect(Collectors.toList());
        }

        return maintenances;
    }

    private LocalDate[] calculatePeriodDates(Integer year, String period) {
        if (period == null) {
            return new LocalDate[]{
                    LocalDate.of(year, 1, 1),
                    LocalDate.of(year, 12, 31)
            };
        }

        return switch (period) {
            case "Q1" -> new LocalDate[]{LocalDate.of(year, 1, 1), LocalDate.of(year, 3, 31)};
            case "Q2" -> new LocalDate[]{LocalDate.of(year, 4, 1), LocalDate.of(year, 6, 30)};
            case "Q3" -> new LocalDate[]{LocalDate.of(year, 7, 1), LocalDate.of(year, 9, 30)};
            case "Q4" -> new LocalDate[]{LocalDate.of(year, 10, 1), LocalDate.of(year, 12, 31)};
            case "H1" -> new LocalDate[]{LocalDate.of(year, 1, 1), LocalDate.of(year, 6, 30)};
            case "H2" -> new LocalDate[]{LocalDate.of(year, 7, 1), LocalDate.of(year, 12, 31)};
            default -> new LocalDate[]{LocalDate.of(year, 1, 1), LocalDate.of(year, 12, 31)};
        };
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

    // ========================================
    // REPORTE FINANCIERO - BUILDERS
    // ========================================

    private FinancialReportResponse.FinancialSummary buildFinancialSummary(
            List<Payment> payments, List<MaintenanceRecord> maintenances) {

        BigDecimal totalIncome = payments.stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal maintenanceExpenses = maintenances.stream()
                .filter(m -> m.getActualCost() != null)
                .map(m -> BigDecimal.valueOf(Double.parseDouble(String.valueOf(m.getActualCost()))))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal netProfit = totalIncome.subtract(maintenanceExpenses);

        Double profitMargin = totalIncome.compareTo(BigDecimal.ZERO) > 0
                ? netProfit.divide(totalIncome, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100)).doubleValue()
                : 0.0;

        return FinancialReportResponse.FinancialSummary.builder()
                .totalIncome(totalIncome)
                .rentIncome(totalIncome)
                .waterIncome(BigDecimal.ZERO)
                .maintenanceExpenses(maintenanceExpenses)
                .netProfit(netProfit)
                .profitMargin(Math.round(profitMargin * 100.0) / 100.0)
                .build();
    }

    private List<FinancialReportResponse.MonthlyIncome> buildMonthlyIncomes(
            List<Payment> payments, List<MaintenanceRecord> maintenances,
            LocalDate startDate, LocalDate endDate) {

        Map<String, FinancialReportResponse.MonthlyIncome> monthlyMap = new LinkedHashMap<>();

        LocalDate current = startDate.withDayOfMonth(1);
        while (!current.isAfter(endDate)) {
            String key = current.getYear() + "-" + current.getMonthValue();
            String monthName = current.getMonth().getDisplayName(TextStyle.SHORT, new Locale("es", "MX"));

            monthlyMap.put(key, FinancialReportResponse.MonthlyIncome.builder()
                    .month(monthName)
                    .year(current.getYear())
                    .income(BigDecimal.ZERO)
                    .expenses(BigDecimal.ZERO)
                    .netProfit(BigDecimal.ZERO)
                    .paymentsCount(0)
                    .build());

            current = current.plusMonths(1);
        }

        for (Payment payment : payments) {
            String key = payment.getPeriodYear() + "-" + payment.getPeriodMonth();
            if (monthlyMap.containsKey(key)) {
                FinancialReportResponse.MonthlyIncome monthly = monthlyMap.get(key);
                BigDecimal income = monthly.getIncome().add(payment.getTotalAmount());
                monthly.setIncome(income);
                monthly.setPaymentsCount(monthly.getPaymentsCount() + 1);
            }
        }

        for (MaintenanceRecord maintenance : maintenances) {
            String key = maintenance.getMaintenanceDate().getYear() + "-" +
                    maintenance.getMaintenanceDate().getMonthValue();
            if (monthlyMap.containsKey(key) && maintenance.getActualCost() != null) {
                FinancialReportResponse.MonthlyIncome monthly = monthlyMap.get(key);
                BigDecimal cost = BigDecimal.valueOf(Double.parseDouble(String.valueOf(maintenance.getActualCost())));
                monthly.setExpenses(monthly.getExpenses().add(cost));
            }
        }

        monthlyMap.values().forEach(monthly ->
                monthly.setNetProfit(monthly.getIncome().subtract(monthly.getExpenses())));

        return new ArrayList<>(monthlyMap.values());
    }

    private List<FinancialReportResponse.PropertyIncome> buildPropertyIncomes(
            List<Payment> payments, List<MaintenanceRecord> maintenances,
            UUID organizationId, FinancialReportRequest request) {

        List<Property> properties;
        if (request.getPropertyId() != null) {
            properties = Collections.singletonList(
                    propertyRepository.findById(request.getPropertyId())
                            .orElseThrow(() -> new IllegalArgumentException("Propiedad no encontrada")));
        } else {
            properties = propertyRepository.findByOrganization_IdAndIsActiveTrue(organizationId);
            if (request.getLocationId() != null) {
                UUID locationId = request.getLocationId();
                properties = properties.stream()
                        .filter(p -> p.getLocation() != null && p.getLocation().getId().equals(locationId))
                        .collect(Collectors.toList());
            }
            if (request.getPropertyType() != null) {
                String type = request.getPropertyType();
                properties = properties.stream()
                        .filter(p -> type.equals(p.getPropertyType()))
                        .collect(Collectors.toList());
            }
        }

        return properties.stream().map(property -> {
                    BigDecimal income = payments.stream()
                            .filter(p -> p.getContract().getProperty().getId().equals(property.getId()))
                            .map(Payment::getTotalAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    BigDecimal expenses = maintenances.stream()
                            .filter(m -> m.getProperty().getId().equals(property.getId()))
                            .filter(m -> m.getActualCost() != null)
                            .map(m -> BigDecimal.valueOf(Double.parseDouble(String.valueOf(m.getActualCost()))))
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    BigDecimal netProfit = income.subtract(expenses);

                    Double roi = income.compareTo(BigDecimal.ZERO) > 0
                            ? netProfit.divide(income, 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100)).doubleValue()
                            : 0.0;

                    return FinancialReportResponse.PropertyIncome.builder()
                            .propertyCode(property.getPropertyCode())
                            .address(property.getAddress())
                            .propertyType(property.getPropertyType())
                            .totalIncome(income)
                            .maintenanceExpenses(expenses)
                            .netProfit(netProfit)
                            .roi(Math.round(roi * 100.0) / 100.0)
                            .build();
                })
                .sorted(Comparator.comparing(FinancialReportResponse.PropertyIncome::getTotalIncome).reversed())
                .collect(Collectors.toList());
    }

    private FinancialReportResponse.PeriodComparison buildPeriodComparison(
            FinancialReportRequest request, UUID organizationId) {

        long daysBetween = ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate());
        LocalDate prevStartDate = request.getStartDate().minusDays(daysBetween + 1);
        LocalDate prevEndDate = request.getStartDate().minusDays(1);

        BigDecimal currentIncome = paymentRepository.findByOrganizationId(organizationId).stream()
                .filter(p -> !p.getDueDate().isBefore(request.getStartDate()) &&
                        !p.getDueDate().isAfter(request.getEndDate()))
                .filter(p -> "PAGADO".equals(p.getStatus()))
                .map(Payment::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal previousIncome = paymentRepository.findByOrganizationId(organizationId).stream()
                .filter(p -> !p.getDueDate().isBefore(prevStartDate) &&
                        !p.getDueDate().isAfter(prevEndDate))
                .filter(p -> "PAGADO".equals(p.getStatus()))
                .map(Payment::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal absoluteChange = currentIncome.subtract(previousIncome);

        Double percentageChange = previousIncome.compareTo(BigDecimal.ZERO) > 0
                ? absoluteChange.divide(previousIncome, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100)).doubleValue()
                : 0.0;

        String trend = absoluteChange.compareTo(BigDecimal.ZERO) > 0 ? "UP"
                : absoluteChange.compareTo(BigDecimal.ZERO) < 0 ? "DOWN" : "STABLE";

        return FinancialReportResponse.PeriodComparison.builder()
                .currentIncome(currentIncome)
                .previousIncome(previousIncome)
                .absoluteChange(absoluteChange)
                .percentageChange(Math.round(percentageChange * 100.0) / 100.0)
                .trend(trend)
                .build();
    }

    // ========================================
    // REPORTE OCUPACIÓN - BUILDERS
    // ========================================

    private OccupancyReportResponse.OccupancySummary buildOccupancySummary(
            List<Property> properties, List<Contract> allContracts,
            LocalDate startDate, LocalDate endDate) {

        int totalProperties = properties.size();
        int rented = (int) properties.stream()
                .filter(p -> "RENTADA".equals(p.getStatus()))
                .count();
        int available = (int) properties.stream()
                .filter(p -> "DISPONIBLE".equals(p.getStatus()))
                .count();
        int maintenance = (int) properties.stream()
                .filter(p -> "MANTENIMIENTO".equals(p.getStatus()))
                .count();

        Double occupancyRate = totalProperties > 0
                ? (rented * 100.0) / totalProperties
                : 0.0;

        int tenantTurnover = (int) allContracts.stream()
                .filter(c -> c.getEndDate() != null)
                .filter(c -> !c.getEndDate().isBefore(startDate) && !c.getEndDate().isAfter(endDate))
                .filter(c -> "VENCIDO".equals(c.getStatus()) || "CANCELADO".equals(c.getStatus()))
                .count();

        return OccupancyReportResponse.OccupancySummary.builder()
                .totalProperties(totalProperties)
                .rentedProperties(rented)
                .availableProperties(available)
                .maintenanceProperties(maintenance)
                .occupancyRate(Math.round(occupancyRate * 100.0) / 100.0)
                .averageDaysToRent(30)
                .tenantTurnover(tenantTurnover)
                .build();
    }

    private List<OccupancyReportResponse.MonthlyOccupancy> buildMonthlyOccupancy(
            List<Property> properties, List<Contract> contracts,
            LocalDate startDate, LocalDate endDate) {

        List<OccupancyReportResponse.MonthlyOccupancy> monthlyList = new ArrayList<>();

        LocalDate current = startDate.withDayOfMonth(1);
        while (!current.isAfter(endDate)) {
            final LocalDate monthDate = current;

            int rentedCount = (int) contracts.stream()
                    .filter(c -> "ACTIVO".equals(c.getStatus()))
                    .filter(c -> !c.getStartDate().isAfter(monthDate) &&
                            (c.getEndDate() == null || !c.getEndDate().isBefore(monthDate)))
                    .count();

            int availableCount = properties.size() - rentedCount;
            double occupancy = properties.size() > 0
                    ? (rentedCount * 100.0) / properties.size()
                    : 0.0;

            String monthName = monthDate.getMonth().getDisplayName(TextStyle.SHORT, new Locale("es", "MX"));

            monthlyList.add(OccupancyReportResponse.MonthlyOccupancy.builder()
                    .month(monthName)
                    .year(monthDate.getYear())
                    .rented(rentedCount)
                    .available(availableCount)
                    .occupancyRate(Math.round(occupancy * 100.0) / 100.0)
                    .build());

            current = current.plusMonths(1);
        }

        return monthlyList;
    }

    private List<OccupancyReportResponse.PropertyOccupancy> buildPropertyOccupancy(
            List<Property> properties, List<Contract> allContracts,
            LocalDate startDate, LocalDate endDate) {

        long totalDays = ChronoUnit.DAYS.between(startDate, endDate) + 1;

        return properties.stream().map(property -> {
                    List<Contract> propertyContracts = allContracts.stream()
                            .filter(c -> c.getProperty().getId().equals(property.getId()))
                            .collect(Collectors.toList());

                    long daysOccupied = propertyContracts.stream()
                            .filter(c -> "ACTIVO".equals(c.getStatus()) || "VENCIDO".equals(c.getStatus()))
                            .mapToLong(c -> {
                                LocalDate contractStart = c.getStartDate().isBefore(startDate) ? startDate : c.getStartDate();
                                LocalDate contractEnd = c.getEndDate() == null || c.getEndDate().isAfter(endDate)
                                        ? endDate : c.getEndDate();

                                if (contractStart.isAfter(endDate) || contractEnd.isBefore(startDate)) {
                                    return 0;
                                }

                                return ChronoUnit.DAYS.between(contractStart, contractEnd) + 1;
                            })
                            .sum();

                    long daysAvailable = totalDays - daysOccupied;
                    double occupancyRate = totalDays > 0 ? (daysOccupied * 100.0) / totalDays : 0.0;

                    return OccupancyReportResponse.PropertyOccupancy.builder()
                            .propertyCode(property.getPropertyCode())
                            .address(property.getAddress())
                            .propertyType(property.getPropertyType())
                            .currentStatus(property.getStatus())
                            .daysOccupied((int) daysOccupied)
                            .daysAvailable((int) daysAvailable)
                            .occupancyRate(Math.round(occupancyRate * 100.0) / 100.0)
                            .contractsCount(propertyContracts.size())
                            .build();
                })
                .sorted(Comparator.comparing(OccupancyReportResponse.PropertyOccupancy::getOccupancyRate).reversed())
                .collect(Collectors.toList());
    }

    // ========================================
    // REPORTE PAGOS - BUILDERS
    // ========================================

    private PaymentReportResponse.PaymentSummary buildPaymentSummary(List<Payment> payments) {
        int total = payments.size();
        int pending = (int) payments.stream().filter(p -> "PENDIENTE".equals(p.getStatus())).count();
        int paid = (int) payments.stream().filter(p -> "PAGADO".equals(p.getStatus())).count();
        int overdue = (int) payments.stream().filter(p -> "ATRASADO".equals(p.getStatus())).count();

        BigDecimal totalExpected = payments.stream()
                .map(Payment::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCollected = payments.stream()
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

        double delinquencyRate = total > 0 ? (overdue * 100.0) / total : 0.0;

        List<Payment> overduePayments = payments.stream()
                .filter(p -> "ATRASADO".equals(p.getStatus()))
                .collect(Collectors.toList());

        double avgDaysOverdue = overduePayments.isEmpty() ? 0.0 : overduePayments.stream()
                .mapToLong(p -> ChronoUnit.DAYS.between(p.getDueDate(), LocalDate.now()))
                .average()
                .orElse(0.0);

        double collectionEfficiency = totalExpected.compareTo(BigDecimal.ZERO) > 0
                ? totalCollected.divide(totalExpected, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100)).doubleValue()
                : 0.0;

        return PaymentReportResponse.PaymentSummary.builder()
                .totalPayments(total)
                .pendingPayments(pending)
                .paidPayments(paid)
                .overduePayments(overdue)
                .totalExpected(totalExpected)
                .totalCollected(totalCollected)
                .pendingAmount(pendingAmount)
                .overdueAmount(overdueAmount)
                .delinquencyRate(Math.round(delinquencyRate * 100.0) / 100.0)
                .averageDaysOverdue(Math.round(avgDaysOverdue * 100.0) / 100.0)
                .collectionEfficiency(Math.round(collectionEfficiency * 100.0) / 100.0)
                .build();
    }

    private List<PaymentReportResponse.PaymentDetail> buildPaymentDetails(List<Payment> payments) {
        return payments.stream()
                .map(payment -> {
                    Integer daysOverdue = null;
                    if ("ATRASADO".equals(payment.getStatus())) {
                        daysOverdue = (int) ChronoUnit.DAYS.between(payment.getDueDate(),
                                payment.getPaymentDate() != null ? payment.getPaymentDate() : LocalDate.now());
                    }

                    String tenantName = payment.getContract().getContractTenants().isEmpty()
                            ? "Sin inquilino"
                            : payment.getContract().getContractTenants().get(0).getTenant().getFullName();

                    return PaymentReportResponse.PaymentDetail.builder()
                            .contractNumber(payment.getContract().getContractNumber())
                            .propertyCode(payment.getContract().getProperty().getPropertyCode())
                            .propertyAddress(payment.getContract().getProperty().getAddress())
                            .tenantName(tenantName)
                            .periodMonth(payment.getPeriodMonth())
                            .periodYear(payment.getPeriodYear())
                            .dueDate(payment.getDueDate())
                            .paymentDate(payment.getPaymentDate())
                            .amount(payment.getTotalAmount())
                            .status(payment.getStatus())
                            .daysOverdue(daysOverdue)
                            .build();
                })
                .sorted(Comparator.comparing(PaymentReportResponse.PaymentDetail::getDueDate).reversed())
                .collect(Collectors.toList());
    }

    private List<PaymentReportResponse.ContractDelinquency> buildTopDelinquents(List<Payment> payments) {
        Map<UUID, List<Payment>> paymentsByContract = payments.stream()
                .filter(p -> "ATRASADO".equals(p.getStatus()))
                .collect(Collectors.groupingBy(p -> p.getContract().getId()));

        return paymentsByContract.entrySet().stream()
                .map(entry -> {
                    Contract contract = entry.getValue().get(0).getContract();
                    List<Payment> overduePayments = entry.getValue();

                    BigDecimal overdueAmount = overduePayments.stream()
                            .map(Payment::getTotalAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    double avgDays = overduePayments.stream()
                            .mapToLong(p -> ChronoUnit.DAYS.between(p.getDueDate(),
                                    p.getPaymentDate() != null ? p.getPaymentDate() : LocalDate.now()))
                            .average()
                            .orElse(0.0);

                    String tenantName = contract.getContractTenants().isEmpty()
                            ? "Sin inquilino"
                            : contract.getContractTenants().get(0).getTenant().getFullName();

                    return PaymentReportResponse.ContractDelinquency.builder()
                            .contractNumber(contract.getContractNumber())
                            .propertyCode(contract.getProperty().getPropertyCode())
                            .tenantName(tenantName)
                            .overdueCount(overduePayments.size())
                            .overdueAmount(overdueAmount)
                            .averageDaysOverdue(Math.round(avgDays * 100.0) / 100.0)
                            .build();
                })
                .sorted(Comparator.comparing(PaymentReportResponse.ContractDelinquency::getOverdueAmount).reversed())
                .limit(5)
                .collect(Collectors.toList());
    }

    private List<PaymentReportResponse.MonthlyDelinquency> buildMonthlyDelinquency(
            List<Payment> payments, LocalDate startDate, LocalDate endDate) {

        Map<String, PaymentReportResponse.MonthlyDelinquency> monthlyMap = new LinkedHashMap<>();

        LocalDate current = startDate.withDayOfMonth(1);
        while (!current.isAfter(endDate)) {
            String key = current.getYear() + "-" + current.getMonthValue();
            String monthName = current.getMonth().getDisplayName(TextStyle.SHORT, new Locale("es", "MX"));

            monthlyMap.put(key, PaymentReportResponse.MonthlyDelinquency.builder()
                    .month(monthName)
                    .year(current.getYear())
                    .overdueCount(0)
                    .overdueAmount(BigDecimal.ZERO)
                    .delinquencyRate(0.0)
                    .build());

            current = current.plusMonths(1);
        }

        Map<String, Long> totalPaymentsByMonth = payments.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getPeriodYear() + "-" + p.getPeriodMonth(),
                        Collectors.counting()
                ));

        Map<String, List<Payment>> overdueByMonth = payments.stream()
                .filter(p -> "ATRASADO".equals(p.getStatus()))
                .collect(Collectors.groupingBy(p -> p.getPeriodYear() + "-" + p.getPeriodMonth()));

        overdueByMonth.forEach((key, overdueList) -> {
            if (monthlyMap.containsKey(key)) {
                PaymentReportResponse.MonthlyDelinquency monthly = monthlyMap.get(key);

                BigDecimal amount = overdueList.stream()
                        .map(Payment::getTotalAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                long totalInMonth = totalPaymentsByMonth.getOrDefault(key, 1L);
                double rate = (overdueList.size() * 100.0) / totalInMonth;

                monthly.setOverdueCount(overdueList.size());
                monthly.setOverdueAmount(amount);
                monthly.setDelinquencyRate(Math.round(rate * 100.0) / 100.0);
            }
        });

        return new ArrayList<>(monthlyMap.values());
    }

    // ========================================
    // REPORTE MANTENIMIENTO - BUILDERS
    // ========================================

    private MaintenanceReportResponse.MaintenanceSummary buildMaintenanceSummary(
            List<MaintenanceRecord> maintenances) {

        int total = maintenances.size();
        int pending = (int) maintenances.stream().filter(m -> "PENDIENTE".equals(m.getStatus())).count();
        int inProgress = (int) maintenances.stream().filter(m -> "EN_PROCESO".equals(m.getStatus())).count();
        int completed = (int) maintenances.stream().filter(m -> "COMPLETADO".equals(m.getStatus())).count();
        int canceled = (int) maintenances.stream().filter(m -> "CANCELADO".equals(m.getStatus())).count();

        BigDecimal totalEstimated = maintenances.stream()
                .filter(m -> m.getEstimatedCost() != null)
                .map(m -> BigDecimal.valueOf(Double.parseDouble(String.valueOf(m.getEstimatedCost()))))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalActual = maintenances.stream()
                .filter(m -> "COMPLETADO".equals(m.getStatus()))
                .filter(m -> m.getActualCost() != null)
                .map(m -> BigDecimal.valueOf(Double.parseDouble(String.valueOf(m.getActualCost()))))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        double costVariance = totalEstimated.compareTo(BigDecimal.ZERO) > 0
                ? totalActual.subtract(totalEstimated).divide(totalEstimated, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100)).doubleValue()
                : 0.0;

        return MaintenanceReportResponse.MaintenanceSummary.builder()
                .totalMaintenances(total)
                .pending(pending)
                .inProgress(inProgress)
                .completed(completed)
                .canceled(canceled)
                .totalEstimatedCost(totalEstimated)
                .totalActualCost(totalActual)
                .costVariance(Math.round(costVariance * 100.0) / 100.0)
                .averageFrequency(0.0)
                .build();
    }

    private MaintenanceReportResponse.TypeBreakdown buildTypeBreakdown(List<MaintenanceRecord> maintenances) {
        int preventive = (int) maintenances.stream().filter(m -> "PREVENTIVO".equals(m.getMaintenanceType())).count();
        int corrective = (int) maintenances.stream().filter(m -> "CORRECTIVO".equals(m.getMaintenanceType())).count();
        int emergency = (int) maintenances.stream().filter(m -> "EMERGENCIA".equals(m.getMaintenanceType())).count();

        BigDecimal preventiveCost = maintenances.stream()
                .filter(m -> "PREVENTIVO".equals(m.getMaintenanceType()))
                .filter(m -> m.getActualCost() != null)
                .map(m -> BigDecimal.valueOf(Double.parseDouble(String.valueOf(m.getActualCost()))))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal correctiveCost = maintenances.stream()
                .filter(m -> "CORRECTIVO".equals(m.getMaintenanceType()))
                .filter(m -> m.getActualCost() != null)
                .map(m -> BigDecimal.valueOf(Double.parseDouble(String.valueOf(m.getActualCost()))))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal emergencyCost = maintenances.stream()
                .filter(m -> "EMERGENCIA".equals(m.getMaintenanceType()))
                .filter(m -> m.getActualCost() != null)
                .map(m -> BigDecimal.valueOf(Double.parseDouble(String.valueOf(m.getActualCost()))))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return MaintenanceReportResponse.TypeBreakdown.builder()
                .preventive(preventive)
                .corrective(corrective)
                .emergency(emergency)
                .preventiveCost(preventiveCost)
                .correctiveCost(correctiveCost)
                .emergencyCost(emergencyCost)
                .build();
    }

    private List<MaintenanceReportResponse.CategoryBreakdown> buildCategoryBreakdown(
            List<MaintenanceRecord> maintenances) {

        BigDecimal totalCost = maintenances.stream()
                .filter(m -> m.getActualCost() != null)
                .map(m -> BigDecimal.valueOf(Double.parseDouble(String.valueOf(m.getActualCost()))))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, List<MaintenanceRecord>> byCategory = maintenances.stream()
                .filter(m -> m.getCategory() != null)
                .collect(Collectors.groupingBy(MaintenanceRecord::getCategory));

        return byCategory.entrySet().stream()
                .map(entry -> {
                    BigDecimal categoryCost = entry.getValue().stream()
                            .filter(m -> m.getActualCost() != null)
                            .map(m -> BigDecimal.valueOf(Double.parseDouble(String.valueOf(m.getActualCost()))))
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    double percentage = totalCost.compareTo(BigDecimal.ZERO) > 0
                            ? categoryCost.divide(totalCost, 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100)).doubleValue()
                            : 0.0;

                    return MaintenanceReportResponse.CategoryBreakdown.builder()
                            .category(entry.getKey())
                            .count(entry.getValue().size())
                            .totalCost(categoryCost)
                            .percentage(Math.round(percentage * 100.0) / 100.0)
                            .build();
                })
                .sorted(Comparator.comparing(MaintenanceReportResponse.CategoryBreakdown::getTotalCost).reversed())
                .collect(Collectors.toList());
    }

    private List<MaintenanceReportResponse.PropertyMaintenance> buildPropertyMaintenances(
            List<MaintenanceRecord> maintenances, UUID organizationId) {

        Map<UUID, List<MaintenanceRecord>> byProperty = maintenances.stream()
                .collect(Collectors.groupingBy(m -> m.getProperty().getId()));

        return byProperty.entrySet().stream()
                .map(entry -> {
                    Property property = entry.getValue().get(0).getProperty();
                    List<MaintenanceRecord> propMaintenances = entry.getValue();

                    BigDecimal estimated = propMaintenances.stream()
                            .filter(m -> m.getEstimatedCost() != null)
                            .map(m -> BigDecimal.valueOf(Double.parseDouble(String.valueOf(m.getEstimatedCost()))))
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    BigDecimal actual = propMaintenances.stream()
                            .filter(m -> m.getActualCost() != null)
                            .map(m -> BigDecimal.valueOf(Double.parseDouble(String.valueOf(m.getActualCost()))))
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    String mostFrequentType = propMaintenances.stream()
                            .collect(Collectors.groupingBy(MaintenanceRecord::getMaintenanceType, Collectors.counting()))
                            .entrySet().stream()
                            .max(Map.Entry.comparingByValue())
                            .map(Map.Entry::getKey)
                            .orElse("N/A");

                    String mostFrequentCategory = propMaintenances.stream()
                            .filter(m -> m.getCategory() != null)
                            .collect(Collectors.groupingBy(MaintenanceRecord::getCategory, Collectors.counting()))
                            .entrySet().stream()
                            .max(Map.Entry.comparingByValue())
                            .map(Map.Entry::getKey)
                            .orElse("N/A");

                    return MaintenanceReportResponse.PropertyMaintenance.builder()
                            .propertyCode(property.getPropertyCode())
                            .address(property.getAddress())
                            .maintenanceCount(propMaintenances.size())
                            .estimatedCost(estimated)
                            .actualCost(actual)
                            .mostFrequentType(mostFrequentType)
                            .mostFrequentCategory(mostFrequentCategory)
                            .build();
                })
                .sorted(Comparator.comparing(MaintenanceReportResponse.PropertyMaintenance::getMaintenanceCount).reversed())
                .collect(Collectors.toList());
    }

    private MaintenanceReportResponse.CostComparison buildCostComparison(List<MaintenanceRecord> maintenances) {
        BigDecimal totalEstimated = maintenances.stream()
                .filter(m -> m.getEstimatedCost() != null)
                .map(m -> BigDecimal.valueOf(Double.parseDouble(String.valueOf(m.getEstimatedCost()))))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalActual = maintenances.stream()
                .filter(m -> "COMPLETADO".equals(m.getStatus()))
                .filter(m -> m.getActualCost() != null)
                .map(m -> BigDecimal.valueOf(Double.parseDouble(String.valueOf(m.getActualCost()))))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal absoluteDiff = totalActual.subtract(totalEstimated);

        double percentageDiff = totalEstimated.compareTo(BigDecimal.ZERO) > 0
                ? absoluteDiff.divide(totalEstimated, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100)).doubleValue()
                : 0.0;

        double accuracy = 100.0 - Math.abs(percentageDiff);

        return MaintenanceReportResponse.CostComparison.builder()
                .totalEstimated(totalEstimated)
                .totalActual(totalActual)
                .absoluteDifference(absoluteDiff)
                .percentageDifference(Math.round(percentageDiff * 100.0) / 100.0)
                .estimationAccuracy(Math.round(accuracy * 100.0) / 100.0)
                .build();
    }

    // ========================================
    // REPORTE EJECUTIVO - BUILDERS (simplified)
    // ========================================

    private ExecutiveReportResponse.ExecutiveKPIs buildExecutiveKPIs(
            UUID organizationId, LocalDate startDate, LocalDate endDate) {
        return ExecutiveReportResponse.ExecutiveKPIs.builder()
                .totalRevenue(BigDecimal.ZERO)
                .totalExpenses(BigDecimal.ZERO)
                .netProfit(BigDecimal.ZERO)
                .profitMargin(0.0)
                .occupancyRate(0.0)
                .delinquencyRate(0.0)
                .collectionEfficiency(0.0)
                .averageROI(0.0)
                .activeProperties(0)
                .activeContracts(0)
                .build();
    }

    private ExecutiveReportResponse.YearComparison buildYearComparison(
            UUID organizationId, LocalDate currentStart, LocalDate currentEnd,
            LocalDate prevStart, LocalDate prevEnd) {
        return ExecutiveReportResponse.YearComparison.builder()
                .currentYearRevenue(BigDecimal.ZERO)
                .previousYearRevenue(BigDecimal.ZERO)
                .revenueGrowth(0.0)
                .currentYearExpenses(BigDecimal.ZERO)
                .previousYearExpenses(BigDecimal.ZERO)
                .expensesGrowth(0.0)
                .currentYearProfit(BigDecimal.ZERO)
                .previousYearProfit(BigDecimal.ZERO)
                .profitGrowth(0.0)
                .currentYearOccupancy(0.0)
                .previousYearOccupancy(0.0)
                .occupancyChange(0.0)
                .build();
    }

    private List<ExecutiveReportResponse.PropertyROI> buildPropertyROIs(
            UUID organizationId, LocalDate startDate, LocalDate endDate, UUID locationId) {
        return new ArrayList<>();
    }

    private List<ExecutiveReportResponse.LocationProfitability> buildLocationProfitability(
            UUID organizationId, LocalDate startDate, LocalDate endDate) {
        return new ArrayList<>();
    }

    private ExecutiveReportResponse.PropertyPerformance buildPropertyPerformance(
            UUID organizationId, LocalDate startDate, LocalDate endDate) {
        return ExecutiveReportResponse.PropertyPerformance.builder()
                .topPerformers(new ArrayList<>())
                .worstPerformers(new ArrayList<>())
                .highestOccupancy(new ArrayList<>())
                .lowestOccupancy(new ArrayList<>())
                .build();
    }
}