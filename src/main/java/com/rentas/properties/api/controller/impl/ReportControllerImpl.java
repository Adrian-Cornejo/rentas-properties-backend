package com.rentas.properties.api.controller.impl;

import com.rentas.properties.api.controller.ReportController;
import com.rentas.properties.api.dto.request.*;
import com.rentas.properties.api.dto.response.*;
import com.rentas.properties.business.services.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Slf4j
public class ReportControllerImpl implements ReportController {

    private final ReportService reportService;

    @Override
    @PostMapping("/financial")
    public ResponseEntity<FinancialReportResponse> generateFinancialReport(
            @Valid @RequestBody FinancialReportRequest request) {
        log.info("POST /api/v1/reports/financial - Generar reporte financiero");

        FinancialReportResponse response = reportService.generateFinancialReport(request);

        log.info("Reporte financiero generado exitosamente para periodo {} - {}",
                request.getStartDate(), request.getEndDate());
        return ResponseEntity.ok(response);
    }

    @Override
    @PostMapping("/occupancy")
    public ResponseEntity<OccupancyReportResponse> generateOccupancyReport(
            @Valid @RequestBody OccupancyReportRequest request) {
        log.info("POST /api/v1/reports/occupancy - Generar reporte de ocupación");

        OccupancyReportResponse response = reportService.generateOccupancyReport(request);

        log.info("Reporte de ocupación generado exitosamente para periodo {} - {}",
                request.getStartDate(), request.getEndDate());
        return ResponseEntity.ok(response);
    }

    @Override
    @PostMapping("/payments")
    public ResponseEntity<PaymentReportResponse> generatePaymentReport(
            @Valid @RequestBody PaymentReportRequest request) {
        log.info("POST /api/v1/reports/payments - Generar reporte de pagos");

        PaymentReportResponse response = reportService.generatePaymentReport(request);

        log.info("Reporte de pagos generado exitosamente para periodo {} - {}",
                request.getStartDate(), request.getEndDate());
        return ResponseEntity.ok(response);
    }

    @Override
    @PostMapping("/maintenance")
    public ResponseEntity<MaintenanceReportResponse> generateMaintenanceReport(
            @Valid @RequestBody MaintenanceReportRequest request) {
        log.info("POST /api/v1/reports/maintenance - Generar reporte de mantenimientos");

        MaintenanceReportResponse response = reportService.generateMaintenanceReport(request);

        log.info("Reporte de mantenimientos generado exitosamente para periodo {} - {}",
                request.getStartDate(), request.getEndDate());
        return ResponseEntity.ok(response);
    }

    @Override
    @PostMapping("/executive")
    public ResponseEntity<ExecutiveReportResponse> generateExecutiveReport(
            @Valid @RequestBody ExecutiveReportRequest request) {
        log.info("POST /api/v1/reports/executive - Generar reporte ejecutivo para año {}", request.getYear());

        ExecutiveReportResponse response = reportService.generateExecutiveReport(request);

        log.info("Reporte ejecutivo generado exitosamente para año {}", request.getYear());
        return ResponseEntity.ok(response);
    }

    @Override
    @PostMapping("/export/pdf")
    public ResponseEntity<byte[]> exportReportToPdf(
            @RequestParam String reportType,
            @RequestBody Object reportData) {
        log.info("POST /api/v1/reports/export/pdf - Exportar reporte {} a PDF", reportType);

        byte[] pdfBytes = reportService.exportReportToPdf(reportType, reportData);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "reporte-" + reportType.toLowerCase() + ".pdf");

        log.info("PDF generado exitosamente para reporte {}", reportType);
        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

    @Override
    @PostMapping("/export/excel")
    public ResponseEntity<byte[]> exportReportToExcel(
            @RequestParam String reportType,
            @RequestBody Object reportData) {
        log.info("POST /api/v1/reports/export/excel - Exportar reporte {} a Excel", reportType);

        byte[] excelBytes = reportService.exportReportToExcel(reportType, reportData);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment", "reporte-" + reportType.toLowerCase() + ".xlsx");

        log.info("Excel generado exitosamente para reporte {}", reportType);
        return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
    }
}