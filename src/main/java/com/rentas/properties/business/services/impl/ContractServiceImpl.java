package com.rentas.properties.business.services.impl;

import com.rentas.properties.api.dto.request.CreateContractRequest;
import com.rentas.properties.api.dto.request.TenantAssignmentDto;
import com.rentas.properties.api.dto.request.UpdateContractRequest;
import com.rentas.properties.api.dto.request.UpdateDepositStatusRequest;
import com.rentas.properties.api.dto.response.ContractDetailResponse;
import com.rentas.properties.api.dto.response.ContractResponse;
import com.rentas.properties.api.dto.response.ContractSummaryResponse;
import com.rentas.properties.api.exception.*;
import com.rentas.properties.business.services.ContractService;
import com.rentas.properties.dao.entity.*;
import com.rentas.properties.dao.repository.*;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContractServiceImpl implements ContractService {

    private final ContractRepository contractRepository;
    private final PropertyRepository propertyRepository;
    private final TenantRepository tenantRepository;
    private final ContractTenantRepository contractTenantRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ContractDetailResponse createContract(CreateContractRequest request) {
        log.info("Creando contrato para propiedad ID: {}", request.getPropertyId());

        User currentUser = getCurrentUser();
        validateUserHasOrganization(currentUser);

        Organization organization = currentUser.getOrganization();

        Property property = propertyRepository.findById(request.getPropertyId())
                .orElseThrow(() -> new PropertyNotFoundException(
                        "Propiedad no encontrada con ID: " + request.getPropertyId()));

        validateUserCanAccessProperty(currentUser, property);

        if (!"DISPONIBLE".equals(property.getStatus())) {
            log.warn("Intento de crear contrato para propiedad {} que no está disponible. Estado actual: {}",
                    property.getId(), property.getStatus());
            throw new PropertyAlreadyRentedException(
                    "La propiedad no está disponible para renta. Estado actual: " + property.getStatus());
        }

        validateTenantsExistAndBelongToOrganization(request.getTenants(), organization.getId());

        validatePrimaryTenant(request.getTenants());

        String contractNumber = generateContractNumber(organization.getId());

        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new InvalidContractDatesException("La fecha de fin debe ser posterior a la fecha de inicio");
        }

        BigDecimal advancePayment = request.getAdvancePayment() != null
                ? request.getAdvancePayment()
                : BigDecimal.ZERO;

        Boolean depositPaid = request.getDepositPaid() != null
                ? request.getDepositPaid()
                : false;

        Contract contract = Contract.builder()
                .organization(organization)
                .property(property)
                .contractNumber(contractNumber)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .signedDate(request.getSignedDate() != null ? request.getSignedDate() : LocalDate.now())
                .monthlyRent(request.getMonthlyRent())
                .waterFee(request.getWaterFee())
                .advancePayment(advancePayment)
                .depositAmount(request.getDepositAmount())
                .depositPaid(depositPaid)
                .depositPaymentDeadline(request.getDepositPaymentDeadline())
                .depositStatus(depositPaid ? "PAGADO" : "PENDIENTE")
                .status("ACTIVO")
                .contractDocumentUrl(request.getContractDocumentUrl())
                .contractDocumentPublicId(request.getContractDocumentPublicId())
                .notes(request.getNotes())
                .isActive(true)
                .build();

        Contract savedContract = contractRepository.save(contract);
        log.info("Contrato creado con ID: {} - Número: {}", savedContract.getId(), savedContract.getContractNumber());

        // Asociar inquilinos al contrato con su información completa
        associateTenantsToContractWithDetails(savedContract, request.getTenants());

        // Cambiar estado de la propiedad a RENTADA
        property.setStatus("RENTADA");
        propertyRepository.save(property);
        log.info("Estado de propiedad {} cambiado a RENTADA", property.getId());

        // Generar pagos automáticos
        generateAutomaticPayments(savedContract);

        return mapToDetailResponse(savedContract);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContractResponse> getAllContracts(boolean includeInactive) {
        log.info("Obteniendo todos los contratos - includeInactive: {}", includeInactive);

        User currentUser = getCurrentUser();
        validateUserHasOrganization(currentUser);

        UUID organizationId = currentUser.getOrganization().getId();

        List<Contract> contracts;
        if (includeInactive) {
            contracts = contractRepository.findByOrganization_Id(organizationId);
        } else {
            contracts = contractRepository.findByOrganization_Id(organizationId).stream()
                    .filter(Contract::getIsActive)
                    .collect(Collectors.toList());
        }

        log.debug("Se encontraron {} contratos", contracts.size());

        return contracts.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ContractDetailResponse getContractById(UUID id) {
        log.info("Obteniendo contrato con ID: {}", id);

        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new ContractNotFoundException("Contrato no encontrado con ID: " + id));

        User currentUser = getCurrentUser();
        validateUserCanAccessContract(currentUser, contract);

        return mapToDetailResponse(contract);
    }

    @Override
    @Transactional(readOnly = true)
    public ContractDetailResponse getContractByNumber(String contractNumber) {
        log.info("Buscando contrato por número: {}", contractNumber);

        Contract contract = contractRepository.findByContractNumber(contractNumber)
                .orElseThrow(() -> new ContractNotFoundException(
                        "Contrato no encontrado con número: " + contractNumber));

        User currentUser = getCurrentUser();
        validateUserCanAccessContract(currentUser, contract);

        return mapToDetailResponse(contract);
    }

    @Override
    @Transactional
    public ContractDetailResponse updateContract(UUID id, UpdateContractRequest request) {
        log.info("Actualizando contrato con ID: {}", id);

        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new ContractNotFoundException("Contrato no encontrado con ID: " + id));

        User currentUser = getCurrentUser();
        validateUserCanAccessContract(currentUser, contract);

        if (request.getSignedDate() != null) {
            contract.setSignedDate(request.getSignedDate());
        }
        if (request.getMonthlyRent() != null) {
            contract.setMonthlyRent(request.getMonthlyRent());
        }
        if (request.getWaterFee() != null) {
            contract.setWaterFee(request.getWaterFee());
        }
        if (request.getDepositPaid() != null) {
            contract.setDepositPaid(request.getDepositPaid());
        }
        if (request.getContractDocumentUrl() != null) {
            contract.setContractDocumentUrl(request.getContractDocumentUrl());
            contract.setContractDocumentPublicId(request.getContractDocumentPublicId());
        }
        if (request.getNotes() != null) {
            contract.setNotes(request.getNotes());
        }
        if (request.getStatus() != null) {
            contract.setStatus(request.getStatus());
        }

        Contract updatedContract = contractRepository.save(contract);
        log.info("Contrato actualizado exitosamente");

        return mapToDetailResponse(updatedContract);
    }

    @Override
    @Transactional
    public void deleteContract(UUID id) {
        log.info("Eliminando contrato con ID: {}", id);

        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new ContractNotFoundException("Contrato no encontrado con ID: " + id));

        User currentUser = getCurrentUser();
        validateUserCanAccessContract(currentUser, contract);

        // Verificar que no tenga pagos pendientes
        long pendingPayments = contract.getPayments().stream()
                .filter(p -> "PENDIENTE".equals(p.getStatus()) || "ATRASADO".equals(p.getStatus()))
                .count();

        if (pendingPayments > 0) {
            log.warn("Intento de eliminar contrato {} con {} pagos pendientes", id, pendingPayments);
            throw new ContractHasPendingPaymentsException(
                    "No se puede eliminar el contrato porque tiene " + pendingPayments + " pagos pendientes");
        }

        // Soft delete
        contract.setIsActive(false);
        contract.setStatus("CANCELADO");
        contractRepository.save(contract);

        // Cambiar estado de la propiedad a DISPONIBLE
        Property property = contract.getProperty();
        property.setStatus("DISPONIBLE");
        propertyRepository.save(property);

        log.info("Contrato eliminado exitosamente (soft delete)");
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContractResponse> getContractsByOrganization(UUID organizationId) {
        log.info("Obteniendo contratos de la organización: {}", organizationId);

        User currentUser = getCurrentUser();
        
        // Solo ADMIN puede ver contratos de otras organizaciones
        if (!"ADMIN".equals(currentUser.getRole()) && 
            !organizationId.equals(currentUser.getOrganization().getId())) {
            throw new UnauthorizedAccessException(
                    "No tienes permisos para acceder a los contratos de esta organización");
        }

        List<Contract> contracts = contractRepository.findByOrganization_Id(organizationId);

        return contracts.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContractResponse> getContractsByStatus(String status) {
        log.info("Obteniendo contratos por estado: {}", status);

        User currentUser = getCurrentUser();
        validateUserHasOrganization(currentUser);

        UUID organizationId = currentUser.getOrganization().getId();

        List<Contract> contracts = contractRepository.findByOrganization_IdAndStatus(organizationId, status);

        return contracts.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContractResponse> getActiveContracts() {
        log.info("Obteniendo contratos activos");

        User currentUser = getCurrentUser();
        validateUserHasOrganization(currentUser);

        UUID organizationId = currentUser.getOrganization().getId();

        List<Contract> contracts = contractRepository.findActiveContractsByOrganization(organizationId);

        return contracts.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContractResponse> getExpiringContracts(int days) {
        log.info("Obteniendo contratos que vencen en {} días", days);

        User currentUser = getCurrentUser();
        validateUserHasOrganization(currentUser);

        UUID organizationId = currentUser.getOrganization().getId();

        LocalDate today = LocalDate.now();
        LocalDate futureDate = today.plusDays(days);

        List<Contract> contracts = contractRepository.findContractsExpiringBetweenByOrganization(
                organizationId, today, futureDate);

        return contracts.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContractResponse> getContractsWithPendingDeposit() {
        log.info("Obteniendo contratos con depósito pendiente");

        User currentUser = getCurrentUser();
        validateUserHasOrganization(currentUser);

        UUID organizationId = currentUser.getOrganization().getId();

        List<Contract> contracts = contractRepository.findContractsWithPendingDepositByOrganization(organizationId);

        return contracts.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ContractDetailResponse getActiveContractByProperty(UUID propertyId) {
        log.info("Obteniendo contrato activo para propiedad ID: {}", propertyId);

        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new PropertyNotFoundException("Propiedad no encontrada con ID: " + propertyId));

        User currentUser = getCurrentUser();
        validateUserCanAccessProperty(currentUser, property);

        Contract contract = contractRepository.findActiveContractByProperty(propertyId)
                .orElseThrow(() -> new ContractNotFoundException(
                        "No hay contrato activo para la propiedad con ID: " + propertyId));

        return mapToDetailResponse(contract);
    }

    @Override
    @Transactional
    public ContractDetailResponse updateDepositStatus(UUID id, UpdateDepositStatusRequest request) {
        log.info("Actualizando estado del depósito para contrato ID: {}", id);

        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new ContractNotFoundException("Contrato no encontrado con ID: " + id));

        User currentUser = getCurrentUser();
        validateUserCanAccessContract(currentUser, contract);

        contract.setDepositStatus(request.getDepositStatus());

        if ("PAGADO".equals(request.getDepositStatus())) {
            contract.setDepositPaid(true);
        }

        if (request.getDepositReturnAmount() != null) {
            contract.setDepositReturnAmount(request.getDepositReturnAmount());
        }

        if (request.getDepositReturnDate() != null) {
            contract.setDepositReturnDate(request.getDepositReturnDate());
        }

        if (request.getDepositDeductionReason() != null) {
            contract.setDepositDeductionReason(request.getDepositDeductionReason());
        }

        Contract updatedContract = contractRepository.save(contract);
        log.info("Estado del depósito actualizado exitosamente a: {}", request.getDepositStatus());

        return mapToDetailResponse(updatedContract);
    }

    @Override
    @Transactional
    public ContractDetailResponse renewContract(UUID id) {
        log.info("Renovando contrato con ID: {}", id);

        Contract oldContract = contractRepository.findById(id)
                .orElseThrow(() -> new ContractNotFoundException("Contrato no encontrado con ID: " + id));

        User currentUser = getCurrentUser();
        validateUserCanAccessContract(currentUser, oldContract);

        // Validar que el contrato puede ser renovado
        if (!"ACTIVO".equals(oldContract.getStatus()) && !"VENCIDO".equals(oldContract.getStatus())) {
            throw new ContractCannotBeRenewedException(
                    "Solo se pueden renovar contratos con estado ACTIVO o VENCIDO");
        }

        // Generar nuevo número de contrato
        String newContractNumber = generateNewContractNumber(oldContract.getContractNumber());

        // Crear nuevo contrato con los mismos datos
        LocalDate newStartDate = oldContract.getEndDate().plusDays(1);
        LocalDate newEndDate = newStartDate.plusMonths(6);

        Contract newContract = Contract.builder()
                .organization(oldContract.getOrganization())
                .property(oldContract.getProperty())
                .contractNumber(newContractNumber)
                .startDate(newStartDate)
                .endDate(newEndDate)
                .signedDate(LocalDate.now())
                .monthlyRent(oldContract.getMonthlyRent())
                .waterFee(oldContract.getWaterFee())
                .advancePayment(oldContract.getMonthlyRent()) // Un mes adelantado
                .depositAmount(oldContract.getDepositAmount())
                .depositPaid(false)
                .depositPaymentDeadline(newStartDate.plusDays(15))
                .depositStatus("PENDIENTE")
                .status("ACTIVO")
                .notes("Renovación del contrato " + oldContract.getContractNumber())
                .isActive(true)
                .build();

        Contract savedNewContract = contractRepository.save(newContract);

        // Copiar los mismos arrendatarios
        List<UUID> tenantIds = oldContract.getContractTenants().stream()
                .map(ct -> ct.getTenant().getId())
                .collect(Collectors.toList());
        associateTenantsToContract(savedNewContract, tenantIds);

        // Marcar el contrato anterior como RENOVADO
        oldContract.setStatus("RENOVADO");
        contractRepository.save(oldContract);

        // Generar pagos automáticos para el nuevo contrato
        generateAutomaticPayments(savedNewContract);

        log.info("Contrato renovado exitosamente. Nuevo contrato ID: {}", savedNewContract.getId());

        return mapToDetailResponse(savedNewContract);
    }

    @Override
    @Transactional
    public ContractDetailResponse cancelContract(UUID id) {
        log.info("Cancelando contrato con ID: {}", id);

        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new ContractNotFoundException("Contrato no encontrado con ID: " + id));

        User currentUser = getCurrentUser();
        validateUserCanAccessContract(currentUser, contract);

        // Validar que el contrato puede ser cancelado
        if ("CANCELADO".equals(contract.getStatus()) || "VENCIDO".equals(contract.getStatus())) {
            throw new ContractCannotBeCancelledException(
                    "No se puede cancelar un contrato con estado: " + contract.getStatus());
        }

        // Cambiar estado a CANCELADO
        contract.setStatus("CANCELADO");
        contract.setIsActive(false);
        contractRepository.save(contract);

        // Liberar la propiedad
        Property property = contract.getProperty();
        property.setStatus("DISPONIBLE");
        propertyRepository.save(property);

        log.info("Contrato cancelado exitosamente");

        return mapToDetailResponse(contract);
    }

    @Override
    @Transactional(readOnly = true)
    public ContractSummaryResponse getContractsSummary() {
        log.info("Obteniendo resumen de contratos");

        User currentUser = getCurrentUser();
        validateUserHasOrganization(currentUser);

        UUID organizationId = currentUser.getOrganization().getId();

        long totalContracts = contractRepository.countActiveByOrganization_Id(organizationId);
        
        List<Contract> activeContracts = contractRepository.findActiveContractsByOrganization(organizationId);
        long totalActive = activeContracts.size();

        List<Contract> expiringContracts = contractRepository.findContractsExpiringBetweenByOrganization(
                organizationId, LocalDate.now(), LocalDate.now().plusDays(30));
        long expiringSoon = expiringContracts.size();

        List<Contract> pendingDepositContracts = 
                contractRepository.findContractsWithPendingDepositByOrganization(organizationId);
        long pendingDeposits = pendingDepositContracts.size();

        // Calcular ingresos mensuales proyectados
        BigDecimal monthlyProjectedIncome = activeContracts.stream()
                .map(c -> c.getMonthlyRent().add(c.getWaterFee()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return ContractSummaryResponse.builder()
                .totalContracts(totalContracts)
                .activeContracts(totalActive)
                .expiringSoonContracts(expiringSoon)
                .pendingDepositsContracts(pendingDeposits)
                .monthlyProjectedIncome(monthlyProjectedIncome)
                .build();
    }

    // ========== MÉTODOS AUXILIARES PRIVADOS ==========

    private User getCurrentUser() {
        String email = ((UserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal()).getUsername();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado: " + email));
    }

    private void validateUserHasOrganization(User user) {
        if (user.getOrganization() == null) {
            throw new UnauthorizedAccessException(
                    "Debes pertenecer a una organización para realizar esta acción");
        }
    }

    private void validateUserCanAccessContract(User user, Contract contract) {
        if (user.getOrganization() == null) {
            throw new UnauthorizedAccessException("No perteneces a ninguna organización");
        }

        if (!contract.getOrganization().getId().equals(user.getOrganization().getId())) {
            log.warn("Usuario {} intentó acceder al contrato {} de otra organización",
                    user.getId(), contract.getId());
            throw new UnauthorizedAccessException(
                    "No tienes permisos para acceder a este contrato");
        }
    }

    private void validateUserCanAccessProperty(User user, Property property) {
        if (user.getOrganization() == null) {
            throw new UnauthorizedAccessException("No perteneces a ninguna organización");
        }

        if (!property.getOrganization().getId().equals(user.getOrganization().getId())) {
            throw new UnauthorizedAccessException(
                    "No tienes permisos para acceder a esta propiedad");
        }
    }

    private void validateTenantsExistAndBelongToOrganization(List<TenantAssignmentDto> tenants, UUID organizationId) {
        for (TenantAssignmentDto tenantDto : tenants) {
            Tenant tenant = tenantRepository.findById(tenantDto.getTenantId())
                    .orElseThrow(() -> new TenantNotFoundException(
                            "Inquilino no encontrado con ID: " + tenantDto.getTenantId()));

            if (!tenant.getOrganization().getId().equals(organizationId)) {
                throw new UnauthorizedAccessException(
                        "El inquilino con ID " + tenantDto.getTenantId() + " no pertenece a tu organización");
            }
        }
    }

    private void associateTenantsToContract(Contract contract, List<UUID> tenantIds) {
        log.info("Asociando {} arrendatarios al contrato {}", tenantIds.size(), contract.getId());

        boolean isFirst = true;
        for (UUID tenantId : tenantIds) {
            Tenant tenant = tenantRepository.findById(tenantId)
                    .orElseThrow(() -> new TenantNotFoundException(
                            "Arrendatario no encontrado con ID: " + tenantId));

            ContractTenant contractTenant = ContractTenant.builder()
                    .contract(contract)
                    .tenant(tenant)
                    .isPrimary(isFirst) // El primero es el responsable principal
                    .relationship(isFirst ? "Titular" : "Co-titular")
                    .build();

            contractTenantRepository.save(contractTenant);
            isFirst = false;
        }

        log.info("Arrendatarios asociados exitosamente");
    }

    private void generateAutomaticPayments(Contract contract) {
        log.info("Generando pagos automáticos para contrato {}", contract.getId());

        LocalDate startDate = contract.getStartDate();
        
        // Pago de adelanto (primer mes)
        Payment advancePayment = Payment.builder()
                .contract(contract)
                .paymentType("ADELANTO")
                .paymentDate(startDate)
                .dueDate(startDate)
                .periodMonth(startDate.getMonthValue())
                .periodYear(startDate.getYear())
                .amount(contract.getAdvancePayment())
                .lateFee(BigDecimal.ZERO)
                .totalAmount(contract.getAdvancePayment())
                .status("PAGADO") // El adelanto se considera pagado al firmar
                .build();
        paymentRepository.save(advancePayment);

        // Generar pagos mensuales (renta + agua) para los siguientes 6 meses
        for (int i = 1; i < 6; i++) {
            LocalDate paymentDueDate = startDate.plusMonths(i);
            
            BigDecimal rentAmount = contract.getMonthlyRent();
            BigDecimal waterAmount = contract.getWaterFee();
            BigDecimal totalAmount = rentAmount.add(waterAmount);

            Payment rentPayment = Payment.builder()
                    .contract(contract)
                    .paymentType("RENTA")
                    .paymentDate(paymentDueDate)
                    .dueDate(paymentDueDate)
                    .periodMonth(paymentDueDate.getMonthValue())
                    .periodYear(paymentDueDate.getYear())
                    .amount(totalAmount)
                    .lateFee(BigDecimal.ZERO)
                    .totalAmount(totalAmount)
                    .status("PENDIENTE")
                    .build();
            paymentRepository.save(rentPayment);
        }

        // Pago del depósito (si no está pagado)
        if (!contract.getDepositPaid()) {
            Payment depositPayment = Payment.builder()
                    .contract(contract)
                    .paymentType("DEPOSITO")
                    .paymentDate(startDate)
                    .dueDate(contract.getDepositPaymentDeadline())
                    .periodMonth(startDate.getMonthValue())
                    .periodYear(startDate.getYear())
                    .amount(contract.getDepositAmount())
                    .lateFee(BigDecimal.ZERO)
                    .totalAmount(contract.getDepositAmount())
                    .status("PENDIENTE")
                    .build();
            paymentRepository.save(depositPayment);
        }

        log.info("Pagos automáticos generados exitosamente");
    }

    private String generateNewContractNumber(String oldContractNumber) {
        // Ej: CONT-2024-001 -> CONT-2024-001-R1
        if (oldContractNumber.contains("-R")) {
            // Ya es una renovación, incrementar el número
            int lastDash = oldContractNumber.lastIndexOf("-R");
            String base = oldContractNumber.substring(0, lastDash);
            String renewalPart = oldContractNumber.substring(lastDash + 2);
            int renewalNumber = Integer.parseInt(renewalPart) + 1;
            return base + "-R" + renewalNumber;
        } else {
            // Primera renovación
            return oldContractNumber + "-R1";
        }
    }

    private String generateContractNumber(UUID organizationId) {
        int currentYear = LocalDate.now().getYear();


        List<Contract> contractsThisYear = contractRepository.findByOrganization_Id(organizationId).stream()
                .filter(c -> c.getCreatedAt().getYear() == currentYear)
                .collect(Collectors.toList());

        int nextNumber = contractsThisYear.size() + 1;

        return String.format("CONT-%d-%03d", currentYear, nextNumber);
    }

    private void validatePrimaryTenant(List<TenantAssignmentDto> tenants) {
        long primaryCount = tenants.stream()
                .filter(TenantAssignmentDto::getIsPrimary)
                .count();

        if (primaryCount == 0) {
            throw new ValidationException("Debe haber exactamente un inquilino principal");
        }

        if (primaryCount > 1) {
            throw new ValidationException("Solo puede haber un inquilino principal");
        }
    }

    private void associateTenantsToContractWithDetails(Contract contract, List<TenantAssignmentDto> tenants) {
        log.info("Asociando {} inquilinos al contrato {}", tenants.size(), contract.getId());

        for (TenantAssignmentDto tenantDto : tenants) {
            Tenant tenant = tenantRepository.findById(tenantDto.getTenantId())
                    .orElseThrow(() -> new TenantNotFoundException(
                            "Inquilino no encontrado con ID: " + tenantDto.getTenantId()));

            ContractTenant contractTenant = ContractTenant.builder()
                    .contract(contract)
                    .tenant(tenant)
                    .isPrimary(tenantDto.getIsPrimary())
                    .relationship(tenantDto.getRelationship())
                    .build();

            contractTenantRepository.save(contractTenant);
        }

        log.info("Inquilinos asociados exitosamente");
    }

    private ContractResponse mapToResponse(Contract contract) {
        // Obtener nombres de arrendatarios
        String tenantNames = contract.getContractTenants().stream()
                .map(ct -> ct.getTenant().getFullName())
                .collect(Collectors.joining(", "));

        return ContractResponse.builder()
                .id(contract.getId())
                .organizationId(contract.getOrganization().getId())
                .propertyId(contract.getProperty().getId())
                .propertyCode(contract.getProperty().getPropertyCode())
                .propertyAddress(contract.getProperty().getAddress())
                .contractNumber(contract.getContractNumber())
                .tenantNames(tenantNames)
                .startDate(contract.getStartDate())
                .endDate(contract.getEndDate())
                .monthlyRent(contract.getMonthlyRent())
                .waterFee(contract.getWaterFee())
                .depositAmount(contract.getDepositAmount())
                .depositPaid(contract.getDepositPaid())
                .depositStatus(contract.getDepositStatus())
                .status(contract.getStatus())
                .isActive(contract.getIsActive())
                .createdAt(contract.getCreatedAt())
                .build();
    }

    private ContractDetailResponse mapToDetailResponse(Contract contract) {
        Property property = contract.getProperty();
        ContractDetailResponse.PropertyDto propertyDto = ContractDetailResponse.PropertyDto.builder()
                .id(property.getId())
                .propertyCode(property.getPropertyCode())
                .address(property.getAddress())
                .propertyType(property.getPropertyType())
                .status(property.getStatus())
                .build();

        List<ContractDetailResponse.TenantDto> tenantDtos = contract.getContractTenants().stream()
                .map(ct -> ContractDetailResponse.TenantDto.builder()
                        .id(ct.getTenant().getId())
                        .fullName(ct.getTenant().getFullName())
                        .phone(ct.getTenant().getPhone())
                        .email(ct.getTenant().getEmail())
                        .isPrimary(ct.getIsPrimary())
                        .relationship(ct.getRelationship())
                        .build())
                .collect(Collectors.toList());

        return ContractDetailResponse.builder()
                .id(contract.getId())
                .organizationId(contract.getOrganization().getId())
                .organizationName(contract.getOrganization().getName())
                .property(propertyDto)
                .tenants(tenantDtos)
                .contractNumber(contract.getContractNumber())
                .startDate(contract.getStartDate())
                .endDate(contract.getEndDate())
                .signedDate(contract.getSignedDate())
                .monthlyRent(contract.getMonthlyRent())
                .waterFee(contract.getWaterFee())
                .advancePayment(contract.getAdvancePayment())
                .depositAmount(contract.getDepositAmount())
                .depositPaid(contract.getDepositPaid())
                .depositPaymentDeadline(contract.getDepositPaymentDeadline())
                .depositStatus(contract.getDepositStatus())
                .depositReturnAmount(contract.getDepositReturnAmount())
                .depositReturnDate(contract.getDepositReturnDate())
                .depositDeductionReason(contract.getDepositDeductionReason())
                .status(contract.getStatus())
                .contractDocumentUrl(contract.getContractDocumentUrl())
                .contractDocumentPublicId(contract.getContractDocumentPublicId())
                .notes(contract.getNotes())
                .isActive(contract.getIsActive())
                .createdAt(contract.getCreatedAt())
                .updatedAt(contract.getUpdatedAt())
                .createdBy(contract.getCreatedBy())
                .build();
    }
}
