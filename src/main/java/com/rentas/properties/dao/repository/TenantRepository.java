package com.rentas.properties.dao.repository;

import com.rentas.properties.dao.entity.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, UUID> {

    Optional<Tenant> findByPhone(String phone);

    Optional<Tenant> findByEmail(String email);

    Optional<Tenant> findByIneNumber(String ineNumber);

    List<Tenant> findByFullNameContainingIgnoreCase(String name);

    List<Tenant> findByIsActiveTrue();

    boolean existsByPhone(String phone);

    boolean existsByEmail(String email);

    List<Tenant> findByOrganization_Id(UUID organizationId);

    List<Tenant> findByOrganization_IdAndIsActiveTrue(UUID organizationId);

    Optional<Tenant> findByPhoneAndOrganization_Id(String phone, UUID organizationId);

    Optional<Tenant> findByEmailAndOrganization_Id(String email, UUID organizationId);

    List<Tenant> findByFullNameContainingIgnoreCaseAndOrganization_Id(String name, UUID organizationId);

    boolean existsByPhoneAndOrganization_Id(String phone, UUID organizationId);

    boolean existsByEmailAndOrganization_Id(String email, UUID organizationId);

    @Query("SELECT COUNT(t) FROM Tenant t WHERE t.organization.id = :organizationId AND t.isActive = true")
    Long countActiveByOrganization_Id(@Param("organizationId") UUID organizationId);
}