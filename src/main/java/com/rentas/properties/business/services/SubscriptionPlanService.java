// src/main/java/com/rentas/properties/service/SubscriptionPlanService.java
package com.rentas.properties.service;

import com.rentas.properties.api.dto.response.SubscriptionPlanResponse;

import java.util.List;
import java.util.UUID;

/**
 * Servicio para gestionar planes de suscripción
 */
public interface SubscriptionPlanService {

    /**
     * Obtener todos los planes activos ordenados
     */
    List<SubscriptionPlanResponse> getAllActivePlans();

    /**
     * Obtener todos los planes (activos e inactivos)
     */
    List<SubscriptionPlanResponse> getAllPlans();

    /**
     * Obtener plan por ID
     */
    SubscriptionPlanResponse getPlanById(UUID planId);

    /**
     * Obtener plan por código (STARTER, BASICO, etc.)
     */
    SubscriptionPlanResponse getPlanByCode(String planCode);

    /**
     * Obtener el plan marcado como "más popular"
     */
    SubscriptionPlanResponse getPopularPlan();

    /**
     * Obtener plan gratuito
     */
    SubscriptionPlanResponse getFreePlan();

    /**
     * Comparar dos planes (para upgrade/downgrade)
     */
    SubscriptionPlanResponse.PlanComparisonResponse comparePlans(UUID currentPlanId, UUID targetPlanId);

    /**
     * Verificar si un plan permite una funcionalidad específica
     */
    boolean planHasFeature(UUID planId, String featureCode);

    /**
     * Obtener estadísticas de uso del plan
     */
    SubscriptionPlanResponse.PlanStatsResponse getPlanStats(UUID planId);
}