package com.rentas.properties.dao.repository;

import com.rentas.properties.dao.entity.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
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
}