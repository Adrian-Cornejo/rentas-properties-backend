package com.rentas.properties.dao.repository;

import com.rentas.properties.dao.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByIsActiveTrue();

    List<User> findByRole(String role);

    Optional<User> findByEmailAndOrganizationId(String email, UUID organizationId);

    List<User> findByOrganizationId(UUID organizationId);

    List<User> findByOrganizationIdAndRole(UUID organizationId, String role);

    List<User> findByOrganizationIdAndAccountStatus(UUID organizationId, String accountStatus);

    List<User> findByAccountStatus(String accountStatus);

    @Query("SELECT u FROM User u WHERE u.organization.id IS NULL AND u.accountStatus = 'pending'")
    List<User> findUsersWithoutOrganization();

    @Query("SELECT COUNT(u) FROM User u WHERE u.organization.id = :organizationId")
    Long countByOrganizationId(@Param("organizationId") UUID organizationId);

    boolean existsByOrganizationIdAndRole(UUID organizationId, String role);
}