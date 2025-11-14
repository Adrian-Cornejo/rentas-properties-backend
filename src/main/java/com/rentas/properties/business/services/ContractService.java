package com.rentas.properties.business.services;

import com.rentas.properties.api.dto.request.CreateContractRequest;
import com.rentas.properties.api.dto.request.UpdateContractRequest;
import com.rentas.properties.api.dto.request.UpdateDepositStatusRequest;
import com.rentas.properties.api.dto.response.ContractDetailResponse;
import com.rentas.properties.api.dto.response.ContractResponse;
import com.rentas.properties.api.dto.response.ContractSummaryResponse;

import java.util.List;
import java.util.UUID;

public interface ContractService {

    ContractDetailResponse createContract(CreateContractRequest request);

    List<ContractResponse> getAllContracts(boolean includeInactive);

    ContractDetailResponse getContractById(UUID id);

    ContractDetailResponse getContractByNumber(String contractNumber);

    ContractDetailResponse updateContract(UUID id, UpdateContractRequest request);

    void deleteContract(UUID id);

    List<ContractResponse> getContractsByOrganization(UUID organizationId);

    List<ContractResponse> getContractsByStatus(String status);

    List<ContractResponse> getActiveContracts();

    List<ContractResponse> getExpiringContracts(int days);

    List<ContractResponse> getContractsWithPendingDeposit();

    ContractDetailResponse getActiveContractByProperty(UUID propertyId);

    ContractDetailResponse updateDepositStatus(UUID id, UpdateDepositStatusRequest request);

    ContractDetailResponse renewContract(UUID id);

    ContractDetailResponse cancelContract(UUID id);

    ContractSummaryResponse getContractsSummary();
}
