// src/main/java/com/rentas/properties/dao/repository/SubscriptionPlanRepository.java
package com.rentas.properties.dao.repository;

import com.rentas.properties.dao.entity.SubscriptionPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, UUID> {

    Optional<SubscriptionPlan> findByPlanCode(String planCode);

    boolean existsByPlanCode(String planCode);

    List<SubscriptionPlan> findByIsActiveTrueOrderByDisplayOrderAsc();

    List<SubscriptionPlan> findAllByOrderByDisplayOrderAsc();

    @Query("SELECT p FROM SubscriptionPlan p WHERE p.monthlyPrice BETWEEN :minPrice AND :maxPrice AND p.isActive = true ORDER BY p.monthlyPrice ASC")
    List<SubscriptionPlan> findByPriceRange(Double minPrice, Double maxPrice);

    Optional<SubscriptionPlan> findByIsPopularTrueAndIsActiveTrue();

    @Query("SELECT p FROM SubscriptionPlan p WHERE p.hasApiAccess = true AND p.isActive = true")
    List<SubscriptionPlan> findPlansWithApiAccess();

    @Query("SELECT p FROM SubscriptionPlan p WHERE p.hasNotifications = true AND p.isActive = true ORDER BY p.displayOrder ASC")
    List<SubscriptionPlan> findPlansWithNotifications();

    @Query("SELECT p FROM SubscriptionPlan p WHERE p.monthlyPrice = 0 AND p.isActive = true")
    Optional<SubscriptionPlan> findFreePlan();


    @Query("SELECT COUNT(o) FROM Organization o WHERE o.subscriptionPlan.id = :planId")
    Long countOrganizationsByPlan(UUID planId);
}