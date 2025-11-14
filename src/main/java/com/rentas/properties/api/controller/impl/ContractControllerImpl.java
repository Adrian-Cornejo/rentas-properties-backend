package com.rentas.properties.api.controller.impl;

import com.rentas.properties.api.controller.ContractController;
import com.rentas.properties.api.dto.request.CreateContractRequest;
import com.rentas.properties.api.dto.request.UpdateContractRequest;
import com.rentas.properties.api.dto.request.UpdateDepositStatusRequest;
import com.rentas.properties.api.dto.response.ContractDetailResponse;
import com.rentas.properties.api.dto.response.ContractResponse;
import com.rentas.properties.api.dto.response.ContractSummaryResponse;
import com.rentas.properties.business.services.ContractService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/contracts")
@RequiredArgsConstructor
@Slf4j
public class ContractControllerImpl implements ContractController {

    private final ContractService contractService;

    @Override
    @PostMapping
    public ResponseEntity<ContractDetailResponse> createContract(@Valid @RequestBody CreateContractRequest request) {
        log.info("Creando nuevo contrato para propiedad ID: {}", request.getPropertyId());
        ContractDetailResponse response = contractService.createContract(request);
        log.info("Contrato creado exitosamente con ID: {} - Número: {}", response.getId(), response.getContractNumber());
        return ResponseEntity.ok(response);
    }

    @Override
    @GetMapping
    public ResponseEntity<List<ContractResponse>> getAllContracts(
            @RequestParam(required = false, defaultValue = "false") boolean includeInactive
    ) {
        log.info("Obteniendo todos los contratos - includeInactive: {}", includeInactive);
        List<ContractResponse> contracts = contractService.getAllContracts(includeInactive);
        log.info("Se encontraron {} contratos", contracts.size());
        return ResponseEntity.ok(contracts);
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<ContractDetailResponse> getContractById(@PathVariable UUID id) {
        log.info("Obteniendo contrato con ID: {}", id);
        ContractDetailResponse response = contractService.getContractById(id);
        log.info("Contrato encontrado: {}", response.getContractNumber());
        return ResponseEntity.ok(response);
    }

    @Override
    @GetMapping("/by-number")
    public ResponseEntity<ContractDetailResponse> getContractByNumber(@RequestParam String contractNumber) {
        log.info("Buscando contrato por número: {}", contractNumber);
        ContractDetailResponse response = contractService.getContractByNumber(contractNumber);
        log.info("Contrato encontrado: {}", response.getContractNumber());
        return ResponseEntity.ok(response);
    }

    @Override
    @PutMapping("/{id}")
    public ResponseEntity<ContractDetailResponse> updateContract(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateContractRequest request
    ) {
        log.info("Actualizando contrato con ID: {}", id);
        ContractDetailResponse response = contractService.updateContract(id, request);
        log.info("Contrato actualizado exitosamente: {}", response.getContractNumber());
        return ResponseEntity.ok(response);
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContract(@PathVariable UUID id) {
        log.info("Eliminando contrato con ID: {}", id);
        contractService.deleteContract(id);
        log.info("Contrato eliminado exitosamente");
        return ResponseEntity.ok().build();
    }

    @Override
    @GetMapping("/organization/{organizationId}")
    public ResponseEntity<List<ContractResponse>> getContractsByOrganization(@PathVariable UUID organizationId) {
        log.info("Obteniendo contratos de la organización: {}", organizationId);
        List<ContractResponse> contracts = contractService.getContractsByOrganization(organizationId);
        log.info("Se encontraron {} contratos para la organización", contracts.size());
        return ResponseEntity.ok(contracts);
    }

    @Override
    @GetMapping("/by-status")
    public ResponseEntity<List<ContractResponse>> getContractsByStatus(@RequestParam String status) {
        log.info("Obteniendo contratos por estado: {}", status);
        List<ContractResponse> contracts = contractService.getContractsByStatus(status);
        log.info("Se encontraron {} contratos con estado {}", contracts.size(), status);
        return ResponseEntity.ok(contracts);
    }

    @Override
    @GetMapping("/active")
    public ResponseEntity<List<ContractResponse>> getActiveContracts() {
        log.info("Obteniendo contratos activos");
        List<ContractResponse> contracts = contractService.getActiveContracts();
        log.info("Se encontraron {} contratos activos", contracts.size());
        return ResponseEntity.ok(contracts);
    }

    @Override
    @GetMapping("/expiring")
    public ResponseEntity<List<ContractResponse>> getExpiringContracts(
            @RequestParam(required = false, defaultValue = "30") int days
    ) {
        log.info("Obteniendo contratos que vencen en {} días", days);
        List<ContractResponse> contracts = contractService.getExpiringContracts(days);
        log.info("Se encontraron {} contratos próximos a vencer", contracts.size());
        return ResponseEntity.ok(contracts);
    }

    @Override
    @GetMapping("/pending-deposit")
    public ResponseEntity<List<ContractResponse>> getContractsWithPendingDeposit() {
        log.info("Obteniendo contratos con depósito pendiente");
        List<ContractResponse> contracts = contractService.getContractsWithPendingDeposit();
        log.info("Se encontraron {} contratos con depósito pendiente", contracts.size());
        return ResponseEntity.ok(contracts);
    }

    @Override
    @GetMapping("/property/{propertyId}/active")
    public ResponseEntity<ContractDetailResponse> getActiveContractByProperty(@PathVariable UUID propertyId) {
        log.info("Obteniendo contrato activo para propiedad ID: {}", propertyId);
        ContractDetailResponse response = contractService.getActiveContractByProperty(propertyId);
        log.info("Contrato activo encontrado: {}", response.getContractNumber());
        return ResponseEntity.ok(response);
    }

    @Override
    @PatchMapping("/{id}/deposit-status")
    public ResponseEntity<ContractDetailResponse> updateDepositStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateDepositStatusRequest request
    ) {
        log.info("Actualizando estado del depósito para contrato ID: {} - Nuevo estado: {}",
                id, request.getDepositStatus());
        ContractDetailResponse response = contractService.updateDepositStatus(id, request);
        log.info("Estado del depósito actualizado exitosamente");
        return ResponseEntity.ok(response);
    }

    @Override
    @PostMapping("/{id}/renew")
    public ResponseEntity<ContractDetailResponse> renewContract(@PathVariable UUID id) {
        log.info("Renovando contrato con ID: {}", id);
        ContractDetailResponse response = contractService.renewContract(id);
        log.info("Contrato renovado exitosamente. Nuevo contrato ID: {}", response.getId());
        return ResponseEntity.ok(response);
    }

    @Override
    @PostMapping("/{id}/cancel")
    public ResponseEntity<ContractDetailResponse> cancelContract(@PathVariable UUID id) {
        log.info("Cancelando contrato con ID: {}", id);
        ContractDetailResponse response = contractService.cancelContract(id);
        log.info("Contrato cancelado exitosamente");
        return ResponseEntity.ok(response);
    }

    @Override
    @GetMapping("/summary")
    public ResponseEntity<ContractSummaryResponse> getContractsSummary() {
        log.info("Obteniendo resumen de contratos");
        ContractSummaryResponse response = contractService.getContractsSummary();
        log.info("Resumen de contratos obtenido exitosamente");
        return ResponseEntity.ok(response);
    }
}