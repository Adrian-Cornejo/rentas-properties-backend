package com.rentas.properties.dao.repository;

import com.rentas.properties.dao.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, UUID> {

    Optional<Organization> findByInvitationCode(String invitationCode);

    boolean existsByInvitationCode(String invitationCode);

    List<Organization> findBySubscriptionStatus(String subscriptionStatus);

    List<Organization> findByIsActiveTrue();

    Optional<Organization> findByOwnerId(UUID ownerId);

    @Query("SELECT o FROM Organization o WHERE o.subscriptionStatus = 'trial' AND o.trialEndsAt < :date")
    List<Organization> findTrialsExpiring(@Param("date") LocalDateTime date);

    @Query("SELECT o FROM Organization o WHERE o.subscriptionStatus = 'active' AND o.subscriptionEndsAt < :date")
    List<Organization> findSubscriptionsExpiring(@Param("date") LocalDateTime date);

    @Query("SELECT o FROM Organization o WHERE o.notificationEnabled = true AND o.isActive = true")
    List<Organization> findOrganizationsWithNotificationsEnabled();

    @Modifying
    @Query("UPDATE Organization o SET o.notificationsSentThisMonth = o.notificationsSentThisMonth + :count " +
            "WHERE o.id = :organizationId")
    void incrementNotificationCount(@Param("organizationId") UUID organizationId, @Param("count") int count);


    @Modifying
    @Query("UPDATE Organization o SET o.notificationsSentThisMonth = 0, o.lastNotificationReset = :resetDate " +
            "WHERE o.lastNotificationReset IS NULL OR o.lastNotificationReset < :resetDate")
    void resetMonthlyNotificationCounters(@Param("resetDate") LocalDate resetDate);
}