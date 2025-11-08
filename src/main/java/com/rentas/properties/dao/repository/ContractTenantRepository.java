package com.rentas.properties.dao.repository;

import com.rentas.properties.dao.entity.ContractTenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ContractTenantRepository extends JpaRepository<ContractTenant, UUID> {

    List<ContractTenant> findByContractId(UUID contractId);

    List<ContractTenant> findByTenantId(UUID tenantId);

    Optional<ContractTenant> findByContractIdAndIsPrimaryTrue(UUID contractId);

    boolean existsByContractIdAndTenantId(UUID contractId, UUID tenantId);

    void deleteByContractId(UUID contractId);
}