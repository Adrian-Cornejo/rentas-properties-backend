package com.rentas.properties.business.services.impl;

import com.rentas.properties.api.dto.request.AddLateFeeRequest;
import com.rentas.properties.api.dto.request.CreatePaymentRequest;
import com.rentas.properties.api.dto.request.MarkAsPaidRequest;
import com.rentas.properties.api.dto.request.UpdatePaymentRequest;
import com.rentas.properties.api.dto.response.PaymentDetailResponse;
import com.rentas.properties.api.dto.response.PaymentResponse;
import com.rentas.properties.api.dto.response.PaymentSummaryResponse;
import com.rentas.properties.api.exception.*;
import com.rentas.properties.business.services.PaymentService;
import com.rentas.properties.dao.entity.Contract;
import com.rentas.properties.dao.entity.Payment;
import com.rentas.properties.dao.entity.User;
import com.rentas.properties.dao.repository.ContractRepository;
import com.rentas.properties.dao.repository.PaymentRepository;
import com.rentas.properties.dao.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final ContractRepository contractRepository;
    private final UserRepository userRepository;

    private static final BigDecimal LATE_FEE_PERCENTAGE = new BigDecimal("0.05"); // 5% por mes
    private static final BigDecimal DAILY_LATE_FEE = new BigDecimal("10.00"); // $10 por día

    @Override
    @Transactional
    public PaymentDetailResponse createPayment(CreatePaymentRequest request) {
        log.info("Creando pago manual para contrato ID: {}", request.getContractId());

        User currentUser = getCurrentUser();
        validateUserHasOrganization(currentUser);

        Contract contract = contractRepository.findById(request.getContractId())
                .orElseThrow(() -> new ContractNotFoundException(
                        "Contrato no encontrado con ID: " + request.getContractId()));

        validateUserCanAccessContract(currentUser, contract);

        // Calcular monto total
        BigDecimal totalAmount = request.getAmount().add(request.getLateFee() != null ? 
                request.getLateFee() : BigDecimal.ZERO);

        Payment payment = Payment.builder()
                .contract(contract)
                .paymentType(request.getPaymentType())
                .paymentDate(request.getPaymentDate())
                .dueDate(request.getDueDate())
                .periodMonth(request.getPeriodMonth())
                .periodYear(request.getPeriodYear())
                .amount(request.getAmount())
                .lateFee(request.getLateFee() != null ? request.getLateFee() : BigDecimal.ZERO)
                .totalAmount(totalAmount)
                .status("PENDIENTE")
                .build();

        Payment savedPayment = paymentRepository.save(payment);
        log.info("Pago creado exitosamente con ID: {}", savedPayment.getId());

        return mapToDetailResponse(savedPayment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getAllPayments() {
        log.info("Obteniendo todos los pagos");

        User currentUser = getCurrentUser();
        validateUserHasOrganization(currentUser);

        UUID organizationId = currentUser.getOrganization().getId();

        List<Payment> payments = paymentRepository.findByOrganizationId(organizationId);

        log.debug("Se encontraron {} pagos", payments.size());

        return payments.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentDetailResponse getPaymentById(UUID id) {
        log.info("Obteniendo pago con ID: {}", id);

        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException("Pago no encontrado con ID: " + id));

        User currentUser = getCurrentUser();
        validateUserCanAccessPayment(currentUser, payment);

        return mapToDetailResponse(payment);
    }

    @Override
    @Transactional
    public PaymentDetailResponse updatePayment(UUID id, UpdatePaymentRequest request) {
        log.info("Actualizando pago con ID: {}", id);

        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException("Pago no encontrado con ID: " + id));

        User currentUser = getCurrentUser();
        validateUserCanAccessPayment(currentUser, payment);

        // Actualizar campos si están presentes
        if (request.getPaymentMethod() != null) {
            payment.setPaymentMethod(request.getPaymentMethod());
        }
        if (request.getReferenceNumber() != null) {
            payment.setReferenceNumber(request.getReferenceNumber());
        }
        if (request.getNotes() != null) {
            payment.setNotes(request.getNotes());
        }

        Payment updatedPayment = paymentRepository.save(payment);
        log.info("Pago actualizado exitosamente");

        return mapToDetailResponse(updatedPayment);
    }

    @Override
    @Transactional
    public PaymentDetailResponse markAsPaid(UUID id, MarkAsPaidRequest request) {
        log.info("Marcando pago {} como pagado", id);

        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException("Pago no encontrado con ID: " + id));

        User currentUser = getCurrentUser();
        validateUserCanAccessPayment(currentUser, payment);

        // Validar que el pago no esté ya pagado
        if ("PAGADO".equals(payment.getStatus())) {
            throw new PaymentAlreadyPaidException("El pago ya está marcado como PAGADO");
        }

        // Si el pago está atrasado y no tiene recargo, calcularlo automáticamente
        if (payment.getDueDate().isBefore(LocalDate.now()) && 
            payment.getLateFee().compareTo(BigDecimal.ZERO) == 0) {
            
            BigDecimal autoLateFee = calculateLateFeeForPayment(payment);
            payment.setLateFee(autoLateFee);
            payment.setTotalAmount(payment.getAmount().add(autoLateFee));
            log.info("Recargo automático calculado: ${}", autoLateFee);
        }

        // Marcar como pagado
        payment.setStatus("PAGADO");
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setReferenceNumber(request.getReferenceNumber());
        payment.setPaidAt(request.getPaidAt() != null ? request.getPaidAt() : LocalDateTime.now());
        payment.setCollectedBy(currentUser);
        
        if (request.getNotes() != null) {
            payment.setNotes(request.getNotes());
        }

        Payment updatedPayment = paymentRepository.save(payment);
        log.info("Pago marcado como PAGADO exitosamente");

        return mapToDetailResponse(updatedPayment);
    }

    @Override
    @Transactional
    public PaymentDetailResponse addLateFee(UUID id, AddLateFeeRequest request) {
        log.info("Agregando recargo por mora al pago {}", id);

        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException("Pago no encontrado con ID: " + id));

        User currentUser = getCurrentUser();
        validateUserCanAccessPayment(currentUser, payment);

        // Validar que el pago no esté pagado
        if ("PAGADO".equals(payment.getStatus())) {
            throw new PaymentAlreadyPaidException(
                    "No se puede agregar recargo a un pago que ya está PAGADO");
        }

        BigDecimal newLateFee;
        
        if (request.isAutoCalculate()) {
            // Calcular recargo automáticamente
            newLateFee = calculateLateFeeForPayment(payment);
            log.info("Recargo calculado automáticamente: ${}", newLateFee);
        } else {
            // Usar monto manual
            newLateFee = request.getLateFeeAmount();
            log.info("Recargo manual aplicado: ${}", newLateFee);
        }

        // Actualizar el pago
        payment.setLateFee(newLateFee);
        payment.setTotalAmount(payment.getAmount().add(newLateFee));
        payment.setStatus("ATRASADO");

        if (request.getReason() != null) {
            String currentNotes = payment.getNotes() != null ? payment.getNotes() + "\n" : "";
            payment.setNotes(currentNotes + "Recargo por mora: " + request.getReason());
        }

        Payment updatedPayment = paymentRepository.save(payment);
        log.info("Recargo agregado exitosamente. Nuevo total: ${}", updatedPayment.getTotalAmount());

        return mapToDetailResponse(updatedPayment);
    }

    @Override
    @Transactional
    public int calculateAutomaticLateFees() {
        log.info("Calculando recargos automáticos para pagos atrasados");

        User currentUser = getCurrentUser();
        validateUserHasOrganization(currentUser);

        UUID organizationId = currentUser.getOrganization().getId();

        // Obtener pagos atrasados que no estén pagados
        List<Payment> overduePayments = paymentRepository.findOverduePayments(LocalDate.now()).stream()
                .filter(p -> p.getContract().getOrganization().getId().equals(organizationId))
                .filter(p -> !"PAGADO".equals(p.getStatus()))
                .collect(Collectors.toList());

        int count = 0;
        for (Payment payment : overduePayments) {
            // Solo aplicar si no tiene recargo o si el recargo es antiguo
            if (payment.getLateFee().compareTo(BigDecimal.ZERO) == 0) {
                BigDecimal lateFee = calculateLateFeeForPayment(payment);
                payment.setLateFee(lateFee);
                payment.setTotalAmount(payment.getAmount().add(lateFee));
                payment.setStatus("ATRASADO");
                paymentRepository.save(payment);
                count++;
                log.debug("Recargo aplicado al pago {}: ${}", payment.getId(), lateFee);
            }
        }

        log.info("Recargos automáticos aplicados a {} pagos", count);
        return count;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByContract(UUID contractId) {
        log.info("Obteniendo pagos del contrato: {}", contractId);

        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new ContractNotFoundException("Contrato no encontrado con ID: " + contractId));

        User currentUser = getCurrentUser();
        validateUserCanAccessContract(currentUser, contract);

        List<Payment> payments = paymentRepository.findByContractId(contractId);

        return payments.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByStatus(String status) {
        log.info("Obteniendo pagos por estado: {}", status);

        User currentUser = getCurrentUser();
        validateUserHasOrganization(currentUser);

        UUID organizationId = currentUser.getOrganization().getId();

        List<Payment> payments = paymentRepository.findByOrganizationIdAndStatus(organizationId, status);

        return payments.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getPendingPayments() {
        log.info("Obteniendo pagos pendientes");

        User currentUser = getCurrentUser();
        validateUserHasOrganization(currentUser);

        UUID organizationId = currentUser.getOrganization().getId();

        List<Payment> payments = paymentRepository.findPendingPaymentsByOrganization(organizationId);

        return payments.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getOverduePayments() {
        log.info("Obteniendo pagos atrasados");

        User currentUser = getCurrentUser();
        validateUserHasOrganization(currentUser);

        UUID organizationId = currentUser.getOrganization().getId();

        List<Payment> payments = paymentRepository.findOverduePayments(LocalDate.now()).stream()
                .filter(p -> p.getContract().getOrganization().getId().equals(organizationId))
                .collect(Collectors.toList());

        return payments.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsDueToday() {
        log.info("Obteniendo pagos que vencen hoy");

        User currentUser = getCurrentUser();
        validateUserHasOrganization(currentUser);

        UUID organizationId = currentUser.getOrganization().getId();

        List<Payment> payments = paymentRepository.findPaymentsDueToday(LocalDate.now()).stream()
                .filter(p -> p.getContract().getOrganization().getId().equals(organizationId))
                .collect(Collectors.toList());

        return payments.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByPeriod(int year, int month) {
        log.info("Obteniendo pagos del periodo {}/{}", month, year);

        // Validar mes
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("El mes debe estar entre 1 y 12");
        }

        User currentUser = getCurrentUser();
        validateUserHasOrganization(currentUser);

        UUID organizationId = currentUser.getOrganization().getId();

        List<Payment> payments = paymentRepository.findByPeriodYearAndPeriodMonth(year, month).stream()
                .filter(p -> p.getContract().getOrganization().getId().equals(organizationId))
                .collect(Collectors.toList());

        return payments.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentSummaryResponse getPaymentsSummary() {
        log.info("Obteniendo resumen de pagos");

        User currentUser = getCurrentUser();
        validateUserHasOrganization(currentUser);

        UUID organizationId = currentUser.getOrganization().getId();

        List<Payment> allPayments = paymentRepository.findByOrganizationId(organizationId);

        long totalPayments = allPayments.size();

        long pendingPayments = allPayments.stream()
                .filter(p -> "PENDIENTE".equals(p.getStatus()))
                .count();

        long overduePayments = allPayments.stream()
                .filter(p -> "ATRASADO".equals(p.getStatus()) || 
                           ("PENDIENTE".equals(p.getStatus()) && p.getDueDate().isBefore(LocalDate.now())))
                .count();

        long paidPayments = allPayments.stream()
                .filter(p -> "PAGADO".equals(p.getStatus()))
                .count();

        // Calcular montos
        BigDecimal totalPendingAmount = allPayments.stream()
                .filter(p -> "PENDIENTE".equals(p.getStatus()) || "ATRASADO".equals(p.getStatus()))
                .map(Payment::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCollected = allPayments.stream()
                .filter(p -> "PAGADO".equals(p.getStatus()))
                .map(Payment::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalLateFees = allPayments.stream()
                .map(Payment::getLateFee)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Ingresos del mes actual
        int currentMonth = LocalDate.now().getMonthValue();
        int currentYear = LocalDate.now().getYear();

        BigDecimal currentMonthIncome = allPayments.stream()
                .filter(p -> "PAGADO".equals(p.getStatus()))
                .filter(p -> p.getPeriodMonth() == currentMonth && p.getPeriodYear() == currentYear)
                .map(Payment::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return PaymentSummaryResponse.builder()
                .totalPayments(totalPayments)
                .pendingPayments(pendingPayments)
                .overduePayments(overduePayments)
                .paidPayments(paidPayments)
                .totalPendingAmount(totalPendingAmount)
                .totalCollectedAmount(totalCollected)
                .totalLateFees(totalLateFees)
                .currentMonthIncome(currentMonthIncome)
                .build();
    }

    // ========== MÉTODOS AUXILIARES PRIVADOS ==========

    private User getCurrentUser() {
        String email = ((UserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal()).getUsername();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + email));
    }

    private void validateUserHasOrganization(User user) {
        if (user.getOrganization() == null) {
            throw new UnauthorizedAccessException(
                    "Debes pertenecer a una organización para realizar esta acción");
        }
    }

    private void validateUserCanAccessPayment(User user, Payment payment) {
        if (user.getOrganization() == null) {
            throw new UnauthorizedAccessException("No perteneces a ninguna organización");
        }

        if (!payment.getContract().getOrganization().getId().equals(user.getOrganization().getId())) {
            log.warn("Usuario {} intentó acceder al pago {} de otra organización",
                    user.getId(), payment.getId());
            throw new UnauthorizedAccessException(
                    "No tienes permisos para acceder a este pago");
        }
    }

    private void validateUserCanAccessContract(User user, Contract contract) {
        if (user.getOrganization() == null) {
            throw new UnauthorizedAccessException("No perteneces a ninguna organización");
        }

        if (!contract.getOrganization().getId().equals(user.getOrganization().getId())) {
            throw new UnauthorizedAccessException(
                    "No tienes permisos para acceder a este contrato");
        }
    }

    /**
     * Calcula el recargo por mora basado en los días de atraso
     * Fórmula: Días de atraso * tarifa diaria
     * O: Porcentaje sobre el monto base
     */
    private BigDecimal calculateLateFeeForPayment(Payment payment) {
        if (!payment.getDueDate().isBefore(LocalDate.now())) {
            return BigDecimal.ZERO;
        }

        long daysOverdue = ChronoUnit.DAYS.between(payment.getDueDate(), LocalDate.now());

        // Opción 1: Recargo fijo por día
        BigDecimal dailyFee = DAILY_LATE_FEE.multiply(BigDecimal.valueOf(daysOverdue));

        // Opción 2: Porcentaje sobre el monto (calculado mensualmente)
        long monthsOverdue = daysOverdue / 30;
        BigDecimal percentageFee = payment.getAmount()
                .multiply(LATE_FEE_PERCENTAGE)
                .multiply(BigDecimal.valueOf(monthsOverdue > 0 ? monthsOverdue : 1));

        // Usar el mayor de los dos
        BigDecimal lateFee = dailyFee.max(percentageFee);

        // Limitar el recargo a un máximo (ej: 50% del monto original)
        BigDecimal maxLateFee = payment.getAmount().multiply(new BigDecimal("0.50"));
        
        if (lateFee.compareTo(maxLateFee) > 0) {
            lateFee = maxLateFee;
        }

        log.debug("Recargo calculado para pago {}: ${} ({} días de atraso)", 
                payment.getId(), lateFee, daysOverdue);

        return lateFee;
    }

    private PaymentResponse mapToResponse(Payment payment) {
        String contractNumber = payment.getContract().getContractNumber();
        String propertyCode = payment.getContract().getProperty().getPropertyCode();
        String propertyAddress = payment.getContract().getProperty().getAddress();

        return PaymentResponse.builder()
                .id(payment.getId())
                .contractId(payment.getContract().getId())
                .contractNumber(contractNumber)
                .propertyCode(propertyCode)
                .propertyAddress(propertyAddress)
                .paymentType(payment.getPaymentType())
                .dueDate(payment.getDueDate())
                .periodMonth(payment.getPeriodMonth())
                .periodYear(payment.getPeriodYear())
                .amount(payment.getAmount())
                .lateFee(payment.getLateFee())
                .totalAmount(payment.getTotalAmount())
                .status(payment.getStatus())
                .paymentMethod(payment.getPaymentMethod())
                .paidAt(payment.getPaidAt())
                .createdAt(payment.getCreatedAt())
                .build();
    }

    private PaymentDetailResponse mapToDetailResponse(Payment payment) {
        Contract contract = payment.getContract();
        
        PaymentDetailResponse.ContractDto contractDto = PaymentDetailResponse.ContractDto.builder()
                .id(contract.getId())
                .contractNumber(contract.getContractNumber())
                .propertyCode(contract.getProperty().getPropertyCode())
                .propertyAddress(contract.getProperty().getAddress())
                .build();

        return PaymentDetailResponse.builder()
                .id(payment.getId())
                .contract(contractDto)
                .paymentType(payment.getPaymentType())
                .paymentDate(payment.getPaymentDate())
                .dueDate(payment.getDueDate())
                .periodMonth(payment.getPeriodMonth())
                .periodYear(payment.getPeriodYear())
                .amount(payment.getAmount())
                .lateFee(payment.getLateFee())
                .totalAmount(payment.getTotalAmount())
                .status(payment.getStatus())
                .paymentMethod(payment.getPaymentMethod())
                .referenceNumber(payment.getReferenceNumber())
                .notes(payment.getNotes())
                .paidAt(payment.getPaidAt())
                .collectedBy(payment.getCollectedBy() != null ? payment.getCollectedBy().getId() : null)
                .collectedByName(payment.getCollectedBy() != null ? 
                        payment.getCollectedBy().getFullName() : null)
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }
}
