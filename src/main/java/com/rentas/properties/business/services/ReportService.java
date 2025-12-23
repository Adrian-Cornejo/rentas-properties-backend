package com.rentas.properties.business.services;

import com.rentas.properties.api.dto.request.*;
import com.rentas.properties.api.dto.response.*;

import java.time.LocalDate;
import java.util.UUID;


public interface ReportService {

    FinancialReportResponse generateFinancialReport(FinancialReportRequest request);

    OccupancyReportResponse generateOccupancyReport(OccupancyReportRequest request);

    PaymentReportResponse generatePaymentReport(PaymentReportRequest request);

    MaintenanceReportResponse generateMaintenanceReport(MaintenanceReportRequest request);

    ExecutiveReportResponse generateExecutiveReport(ExecutiveReportRequest request);

    void validateDateRangeForPlan(LocalDate startDate, LocalDate endDate, UUID organizationId);

    byte[] exportReportToPdf(String reportType, Object reportData);


    byte[] exportReportToExcel(String reportType, Object reportData);
}