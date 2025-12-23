package com.rentas.properties.api.controller;

import com.rentas.properties.api.dto.request.*;
import com.rentas.properties.api.dto.response.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Reports", description = "API para generación de reportes analíticos")
public interface ReportController {

    @Operation(
            summary = "Generar reporte financiero",
            description = "Genera un reporte detallado de ingresos, gastos y rentabilidad. " +
                    "El rango de fechas debe estar dentro del límite del plan de suscripción."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Reporte generado exitosamente",
                    content = @Content(schema = @Schema(implementation = FinancialReportResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Rango de fechas inválido o fuera del límite del plan"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "No tienes permisos")
    })
    ResponseEntity<FinancialReportResponse> generateFinancialReport(
            @Valid @RequestBody FinancialReportRequest request
    );

    @Operation(
            summary = "Generar reporte de ocupación",
            description = "Genera un reporte de ocupación de propiedades, tasas y tendencias. " +
                    "El rango de fechas debe estar dentro del límite del plan de suscripción."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Reporte generado exitosamente",
                    content = @Content(schema = @Schema(implementation = OccupancyReportResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Rango de fechas inválido o fuera del límite del plan"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "No tienes permisos")
    })
    ResponseEntity<OccupancyReportResponse> generateOccupancyReport(
            @Valid @RequestBody OccupancyReportRequest request
    );

    @Operation(
            summary = "Generar reporte de pagos y morosidad",
            description = "Genera un reporte detallado de pagos, morosidad y eficiencia de cobro. " +
                    "El rango de fechas debe estar dentro del límite del plan de suscripción."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Reporte generado exitosamente",
                    content = @Content(schema = @Schema(implementation = PaymentReportResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Rango de fechas inválido o fuera del límite del plan"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "No tienes permisos")
    })
    ResponseEntity<PaymentReportResponse> generatePaymentReport(
            @Valid @RequestBody PaymentReportRequest request
    );

    @Operation(
            summary = "Generar reporte de mantenimientos",
            description = "Genera un reporte de mantenimientos, costos y frecuencias. " +
                    "El rango de fechas debe estar dentro del límite del plan de suscripción."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Reporte generado exitosamente",
                    content = @Content(schema = @Schema(implementation = MaintenanceReportResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Rango de fechas inválido o fuera del límite del plan"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "No tienes permisos")
    })
    ResponseEntity<MaintenanceReportResponse> generateMaintenanceReport(
            @Valid @RequestBody MaintenanceReportRequest request
    );

    @Operation(
            summary = "Generar reporte ejecutivo/comparativo",
            description = "Genera un reporte ejecutivo con KPIs, comparativas y análisis de rendimiento. " +
                    "**Requiere plan PROFESIONAL o EMPRESARIAL (hasAdvancedReports).**"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Reporte generado exitosamente",
                    content = @Content(schema = @Schema(implementation = ExecutiveReportResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Año inválido"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "No tienes permisos o tu plan no incluye reportes avanzados")
    })
    ResponseEntity<ExecutiveReportResponse> generateExecutiveReport(
            @Valid @RequestBody ExecutiveReportRequest request
    );

    @Operation(
            summary = "Exportar reporte a PDF",
            description = "Exporta un reporte previamente generado a formato PDF. " +
                    "**Requiere plan con hasPdfReports (BÁSICO, PROFESIONAL o EMPRESARIAL).**"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "PDF generado exitosamente",
                    content = @Content(mediaType = "application/pdf")
            ),
            @ApiResponse(responseCode = "400", description = "Tipo de reporte inválido"),
            @ApiResponse(responseCode = "403", description = "Tu plan no incluye exportación a PDF")
    })
    ResponseEntity<byte[]> exportReportToPdf(
            @Parameter(description = "Tipo de reporte", example = "FINANCIAL")
            @RequestParam String reportType,
            @RequestBody Object reportData
    );

    @Operation(
            summary = "Exportar reporte a Excel",
            description = "Exporta un reporte previamente generado a formato Excel. " +
                    "**Requiere plan con hasDataExport (PROFESIONAL o EMPRESARIAL).**"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Excel generado exitosamente",
                    content = @Content(mediaType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
            ),
            @ApiResponse(responseCode = "400", description = "Tipo de reporte inválido"),
            @ApiResponse(responseCode = "403", description = "Tu plan no incluye exportación de datos")
    })
    ResponseEntity<byte[]> exportReportToExcel(
            @Parameter(description = "Tipo de reporte", example = "FINANCIAL")
            @RequestParam String reportType,
            @RequestBody Object reportData
    );
}