package com.rentas.properties.dao.repository;

import com.rentas.properties.dao.entity.Contract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ContractRepository extends JpaRepository<Contract, UUID> {

    Optional<Contract> findByContractNumber(String contractNumber);

    List<Contract> findByStatus(String status);

    List<Contract> findByPropertyId(UUID propertyId);

    @Query("SELECT c FROM Contract c WHERE c.property.id = :propertyId AND c.status = 'ACTIVO'")
    Optional<Contract> findActiveContractByProperty(@Param("propertyId") UUID propertyId);

    @Query("SELECT c FROM Contract c WHERE c.endDate BETWEEN :startDate AND :endDate AND c.status = 'ACTIVO'")
    List<Contract> findContractsExpiringBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT c FROM Contract c WHERE c.depositPaid = false AND c.status = 'ACTIVO'")
    List<Contract> findContractsWithPendingDeposit();

    boolean existsByContractNumber(String contractNumber);

    List<Contract> findByOrganization_Id(UUID organizationId);

    List<Contract> findByOrganization_IdAndStatus(UUID organizationId, String status);

    @Query("SELECT c FROM Contract c WHERE c.organization.id = :organizationId AND c.status = 'ACTIVO'")
    List<Contract> findActiveContractsByOrganization(@Param("organizationId") UUID organizationId);

    @Query("SELECT COUNT(c) FROM Contract c WHERE c.organization.id = :organizationId AND c.status = 'ACTIVO'")
    Long countActiveByOrganization_Id(@Param("organizationId") UUID organizationId);

    @Query("SELECT c FROM Contract c WHERE c.organization.id = :organizationId " +
            "AND c.endDate BETWEEN :startDate AND :endDate AND c.status = 'ACTIVO'")
    List<Contract> findContractsExpiringBetweenByOrganization(
            @Param("organizationId") UUID organizationId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("SELECT c FROM Contract c WHERE c.organization.id = :organizationId " +
            "AND c.depositPaid = false AND c.status = 'ACTIVO'")
    List<Contract> findContractsWithPendingDepositByOrganization(@Param("organizationId") UUID organizationId);
}