package com.rentas.properties.api.controller.impl;

import com.rentas.properties.api.controller.PaymentController;
import com.rentas.properties.api.dto.request.AddLateFeeRequest;
import com.rentas.properties.api.dto.request.CreatePaymentRequest;
import com.rentas.properties.api.dto.request.MarkAsPaidRequest;
import com.rentas.properties.api.dto.request.UpdatePaymentRequest;
import com.rentas.properties.api.dto.response.PaymentDetailResponse;
import com.rentas.properties.api.dto.response.PaymentResponse;
import com.rentas.properties.api.dto.response.PaymentSummaryResponse;
import com.rentas.properties.business.services.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentControllerImpl implements PaymentController {

    private final PaymentService paymentService;

    @Override
    @PostMapping
    public ResponseEntity<PaymentDetailResponse> createPayment(@Valid @RequestBody CreatePaymentRequest request) {
        log.info("Creando nuevo pago para contrato ID: {}", request.getContractId());
        PaymentDetailResponse response = paymentService.createPayment(request);
        log.info("Pago creado exitosamente con ID: {}", response.getId());
        return ResponseEntity.ok(response);
    }

    @Override
    @GetMapping
    public ResponseEntity<List<PaymentResponse>> getAllPayments() {
        log.info("Obteniendo todos los pagos");
        List<PaymentResponse> payments = paymentService.getAllPayments();
        log.info("Se encontraron {} pagos", payments.size());
        return ResponseEntity.ok(payments);
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<PaymentDetailResponse> getPaymentById(@PathVariable UUID id) {
        log.info("Obteniendo pago con ID: {}", id);
        PaymentDetailResponse response = paymentService.getPaymentById(id);
        log.info("Pago encontrado: Tipo {} - Periodo {}/{}", 
                response.getPaymentType(), response.getPeriodMonth(), response.getPeriodYear());
        return ResponseEntity.ok(response);
    }

    @Override
    @PutMapping("/{id}")
    public ResponseEntity<PaymentDetailResponse> updatePayment(
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePaymentRequest request
    ) {
        log.info("Actualizando pago con ID: {}", id);
        PaymentDetailResponse response = paymentService.updatePayment(id, request);
        log.info("Pago actualizado exitosamente");
        return ResponseEntity.ok(response);
    }

    @Override
    @PostMapping("/{id}/mark-as-paid")
    public ResponseEntity<PaymentDetailResponse> markAsPaid(
            @PathVariable UUID id,
            @Valid @RequestBody MarkAsPaidRequest request
    ) {
        log.info("Marcando pago {} como pagado", id);
        PaymentDetailResponse response = paymentService.markAsPaid(id, request);
        log.info("Pago marcado como PAGADO exitosamente");
        return ResponseEntity.ok(response);
    }

    @Override
    @PostMapping("/{id}/add-late-fee")
    public ResponseEntity<PaymentDetailResponse> addLateFee(
            @PathVariable UUID id,
            @Valid @RequestBody AddLateFeeRequest request
    ) {
        log.info("Agregando recargo por mora al pago {}", id);
        PaymentDetailResponse response = paymentService.addLateFee(id, request);
        log.info("Recargo agregado exitosamente: ${}", request.getLateFeeAmount());
        return ResponseEntity.ok(response);
    }

    @Override
    @PostMapping("/calculate-late-fees")
    public ResponseEntity<String> calculateAutomaticLateFees() {
        log.info("Calculando recargos automáticos por mora");
        int count = paymentService.calculateAutomaticLateFees();
        log.info("Recargos aplicados a {} pagos", count);
        return ResponseEntity.ok(String.format("Se aplicaron recargos a %d pagos atrasados", count));
    }

    @Override
    @GetMapping("/contract/{contractId}")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByContract(@PathVariable UUID contractId) {
        log.info("Obteniendo pagos del contrato: {}", contractId);
        List<PaymentResponse> payments = paymentService.getPaymentsByContract(contractId);
        log.info("Se encontraron {} pagos para el contrato", payments.size());
        return ResponseEntity.ok(payments);
    }

    @Override
    @GetMapping("/by-status")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByStatus(@RequestParam String status) {
        log.info("Obteniendo pagos por estado: {}", status);
        List<PaymentResponse> payments = paymentService.getPaymentsByStatus(status);
        log.info("Se encontraron {} pagos con estado {}", payments.size(), status);
        return ResponseEntity.ok(payments);
    }

    @Override
    @GetMapping("/pending")
    public ResponseEntity<List<PaymentResponse>> getPendingPayments() {
        log.info("Obteniendo pagos pendientes");
        List<PaymentResponse> payments = paymentService.getPendingPayments();
        log.info("Se encontraron {} pagos pendientes", payments.size());
        return ResponseEntity.ok(payments);
    }

    @Override
    @GetMapping("/overdue")
    public ResponseEntity<List<PaymentResponse>> getOverduePayments() {
        log.info("Obteniendo pagos atrasados");
        List<PaymentResponse> payments = paymentService.getOverduePayments();
        log.info("Se encontraron {} pagos atrasados", payments.size());
        return ResponseEntity.ok(payments);
    }

    @Override
    @GetMapping("/due-today")
    public ResponseEntity<List<PaymentResponse>> getPaymentsDueToday() {
        log.info("Obteniendo pagos que vencen hoy");
        List<PaymentResponse> payments = paymentService.getPaymentsDueToday();
        log.info("Se encontraron {} pagos que vencen hoy", payments.size());
        return ResponseEntity.ok(payments);
    }

    @Override
    @GetMapping("/by-period")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByPeriod(
            @RequestParam int year,
            @RequestParam int month
    ) {
        log.info("Obteniendo pagos del periodo {}/{}", month, year);
        List<PaymentResponse> payments = paymentService.getPaymentsByPeriod(year, month);
        log.info("Se encontraron {} pagos para el periodo", payments.size());
        return ResponseEntity.ok(payments);
    }

    @Override
    @GetMapping("/summary")
    public ResponseEntity<PaymentSummaryResponse> getPaymentsSummary() {
        log.info("Obteniendo resumen de pagos");
        PaymentSummaryResponse response = paymentService.getPaymentsSummary();
        log.info("Resumen de pagos obtenido exitosamente");
        return ResponseEntity.ok(response);
    }
}
