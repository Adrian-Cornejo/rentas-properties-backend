package com.rentas.properties.api.controller;

import com.rentas.properties.api.dto.request.AddLateFeeRequest;
import com.rentas.properties.api.dto.request.CreatePaymentRequest;
import com.rentas.properties.api.dto.request.MarkAsPaidRequest;
import com.rentas.properties.api.dto.request.UpdatePaymentRequest;
import com.rentas.properties.api.dto.response.PaymentDetailResponse;
import com.rentas.properties.api.dto.response.PaymentResponse;
import com.rentas.properties.api.dto.response.PaymentSummaryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

@Tag(name = "Payments", description = "Endpoints para gestión de pagos mensuales (renta + agua)")
public interface PaymentController {

    @Operation(
            summary = "Crear nuevo pago",
            description = "Crea un nuevo registro de pago manualmente. Los pagos se generan automáticamente al crear contratos."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Pago creado exitosamente",
                    content = @Content(schema = @Schema(implementation = PaymentDetailResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Contrato no encontrado"),
            @ApiResponse(responseCode = "403", description = "No tienes permisos")
    })
    ResponseEntity<PaymentDetailResponse> createPayment(@Valid @RequestBody CreatePaymentRequest request);

    @Operation(
            summary = "Obtener todos los pagos",
            description = "Lista todos los pagos de la organización del usuario"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista obtenida exitosamente",
                    content = @Content(schema = @Schema(implementation = PaymentResponse.class))
            )
    })
    ResponseEntity<List<PaymentResponse>> getAllPayments();

    @Operation(
            summary = "Obtener pago por ID",
            description = "Obtiene los detalles de un pago específico"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Pago encontrado",
                    content = @Content(schema = @Schema(implementation = PaymentDetailResponse.class))
            ),
            @ApiResponse(responseCode = "404", description = "Pago no encontrado"),
            @ApiResponse(responseCode = "403", description = "No tienes acceso a este pago")
    })
    ResponseEntity<PaymentDetailResponse> getPaymentById(@PathVariable UUID id);

    @Operation(
            summary = "Actualizar información del pago",
            description = "Actualiza información básica del pago (no cambia el estado)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Pago actualizado exitosamente",
                    content = @Content(schema = @Schema(implementation = PaymentDetailResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Pago no encontrado"),
            @ApiResponse(responseCode = "403", description = "No tienes permisos")
    })
    ResponseEntity<PaymentDetailResponse> updatePayment(
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePaymentRequest request
    );

    @Operation(
            summary = "Marcar pago como pagado",
            description = "Marca un pago como PAGADO y registra información del pago realizado"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Pago marcado como pagado exitosamente",
                    content = @Content(schema = @Schema(implementation = PaymentDetailResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "El pago ya está pagado o datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Pago no encontrado"),
            @ApiResponse(responseCode = "403", description = "No tienes permisos")
    })
    ResponseEntity<PaymentDetailResponse> markAsPaid(
            @PathVariable UUID id,
            @Valid @RequestBody MarkAsPaidRequest request
    );

    @Operation(
            summary = "Agregar recargo por mora",
            description = "Calcula y agrega un recargo por mora al pago. El recargo puede ser automático o manual."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Recargo agregado exitosamente",
                    content = @Content(schema = @Schema(implementation = PaymentDetailResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "El pago ya está pagado o datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Pago no encontrado"),
            @ApiResponse(responseCode = "403", description = "No tienes permisos")
    })
    ResponseEntity<PaymentDetailResponse> addLateFee(
            @PathVariable UUID id,
            @Valid @RequestBody AddLateFeeRequest request
    );

    @Operation(
            summary = "Calcular recargos automáticos",
            description = "Calcula y aplica recargos por mora a todos los pagos atrasados de la organización"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Recargos calculados y aplicados exitosamente"
            )
    })
    ResponseEntity<String> calculateAutomaticLateFees();

    @Operation(
            summary = "Obtener pagos por contrato",
            description = "Lista todos los pagos de un contrato específico"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista obtenida exitosamente",
                    content = @Content(schema = @Schema(implementation = PaymentResponse.class))
            ),
            @ApiResponse(responseCode = "404", description = "Contrato no encontrado")
    })
    ResponseEntity<List<PaymentResponse>> getPaymentsByContract(@PathVariable UUID contractId);

    @Operation(
            summary = "Obtener pagos por estado",
            description = "Filtra pagos por estado (PENDIENTE, PAGADO, ATRASADO, PARCIAL)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista obtenida exitosamente",
                    content = @Content(schema = @Schema(implementation = PaymentResponse.class))
            )
    })
    ResponseEntity<List<PaymentResponse>> getPaymentsByStatus(@RequestParam String status);

    @Operation(
            summary = "Obtener pagos pendientes",
            description = "Lista todos los pagos con estado PENDIENTE de la organización"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista obtenida exitosamente",
                    content = @Content(schema = @Schema(implementation = PaymentResponse.class))
            )
    })
    ResponseEntity<List<PaymentResponse>> getPendingPayments();

    @Operation(
            summary = "Obtener pagos atrasados",
            description = "Lista todos los pagos vencidos (fecha de vencimiento pasada y estado PENDIENTE o ATRASADO)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista obtenida exitosamente",
                    content = @Content(schema = @Schema(implementation = PaymentResponse.class))
            )
    })
    ResponseEntity<List<PaymentResponse>> getOverduePayments();

    @Operation(
            summary = "Obtener pagos que vencen hoy",
            description = "Lista pagos con fecha de vencimiento igual a hoy y estado PENDIENTE"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista obtenida exitosamente",
                    content = @Content(schema = @Schema(implementation = PaymentResponse.class))
            )
    })
    ResponseEntity<List<PaymentResponse>> getPaymentsDueToday();

    @Operation(
            summary = "Obtener pagos por periodo",
            description = "Lista pagos de un mes/año específico"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista obtenida exitosamente",
                    content = @Content(schema = @Schema(implementation = PaymentResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Periodo inválido")
    })
    ResponseEntity<List<PaymentResponse>> getPaymentsByPeriod(
            @Parameter(description = "Año (ej: 2024)")
            @RequestParam int year,
            @Parameter(description = "Mes (1-12)")
            @RequestParam int month
    );

    @Operation(
            summary = "Obtener resumen de pagos",
            description = "Obtiene estadísticas y resumen de pagos de la organización"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Resumen obtenido exitosamente",
                    content = @Content(schema = @Schema(implementation = PaymentSummaryResponse.class))
            )
    })
    ResponseEntity<PaymentSummaryResponse> getPaymentsSummary();
}
