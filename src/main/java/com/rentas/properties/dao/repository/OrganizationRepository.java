package com.rentas.properties.dao.repository;

import com.rentas.properties.dao.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, UUID> {

    Optional<Organization> findByInvitationCode(String invitationCode);

    boolean existsByInvitationCode(String invitationCode);

    List<Organization> findBySubscriptionStatus(String subscriptionStatus);

    List<Organization> findBySubscriptionPlan(String subscriptionPlan);

    List<Organization> findByIsActiveTrue();

    Optional<Organization> findByOwnerId(UUID ownerId);

    @Query("SELECT o FROM Organization o WHERE o.subscriptionStatus = 'trial' AND o.trialEndsAt < :date")
    List<Organization> findTrialsExpiring(@Param("date") LocalDateTime date);

    @Query("SELECT o FROM Organization o WHERE o.subscriptionStatus = 'active' AND o.subscriptionEndsAt < :date")
    List<Organization> findSubscriptionsExpiring(@Param("date") LocalDateTime date);

    @Query("SELECT o FROM Organization o WHERE o.currentUsersCount >= o.maxUsers")
    List<Organization> findOrganizationsAtUserLimit();

    @Query("SELECT o FROM Organization o WHERE o.currentPropertiesCount >= o.maxProperties")
    List<Organization> findOrganizationsAtPropertyLimit();
}