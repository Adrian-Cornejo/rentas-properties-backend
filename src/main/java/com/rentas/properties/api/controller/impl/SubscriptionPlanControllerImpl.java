// src/main/java/com/rentas/properties/api/controller/SubscriptionPlanController.java
package com.rentas.properties.api.controller.impl;

import com.rentas.properties.api.controller.SubscriptionPlanController;
import com.rentas.properties.api.dto.response.SubscriptionPlanResponse;
import com.rentas.properties.service.SubscriptionPlanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/subscription-plans")
@RequiredArgsConstructor
@Slf4j
public class SubscriptionPlanControllerImpl implements SubscriptionPlanController {

    private final SubscriptionPlanService subscriptionPlanService;

    @Override
    public ResponseEntity<List<SubscriptionPlanResponse>> getAllActivePlans() {
        log.info("GET /api/v1/subscription-plans - Obtener todos los planes activos");

        List<SubscriptionPlanResponse> plans = subscriptionPlanService.getAllActivePlans();

        log.info("Retornando {} planes activos", plans.size());
        return ResponseEntity.ok(plans);
    }

    @Override
    public ResponseEntity<List<SubscriptionPlanResponse>> getAllPlans() {
        log.info("GET /api/v1/subscription-plans/all - Obtener todos los planes");

        List<SubscriptionPlanResponse> plans = subscriptionPlanService.getAllPlans();

        return ResponseEntity.ok(plans);
    }

    @Override
    public ResponseEntity<SubscriptionPlanResponse> getPlanById(UUID planId) {
        log.info("GET /api/v1/subscription-plans/{} - Obtener plan por ID", planId);

        SubscriptionPlanResponse plan = subscriptionPlanService.getPlanById(planId);

        return ResponseEntity.ok(plan);
    }

    @Override
    public ResponseEntity<SubscriptionPlanResponse> getPlanByCode(String planCode) {
        log.info("GET /api/v1/subscription-plans/code/{} - Obtener plan por código", planCode);

        SubscriptionPlanResponse plan = subscriptionPlanService.getPlanByCode(planCode.toUpperCase());

        return ResponseEntity.ok(plan);
    }

    @Override
    public ResponseEntity<SubscriptionPlanResponse> getPopularPlan() {
        log.info("GET /api/v1/subscription-plans/popular - Obtener plan popular");

        SubscriptionPlanResponse plan = subscriptionPlanService.getPopularPlan();

        return ResponseEntity.ok(plan);
    }

    @Override
    public ResponseEntity<SubscriptionPlanResponse> getFreePlan() {
        log.info("GET /api/v1/subscription-plans/free - Obtener plan gratuito");

        SubscriptionPlanResponse plan = subscriptionPlanService.getFreePlan();

        return ResponseEntity.ok(plan);
    }

    @Override
    public ResponseEntity<SubscriptionPlanResponse.PlanComparisonResponse> comparePlans(
            UUID currentPlanId, UUID targetPlanId) {

        log.info("GET /api/v1/subscription-plans/compare?currentPlanId={}&targetPlanId={}",
                currentPlanId, targetPlanId);

        SubscriptionPlanResponse.PlanComparisonResponse comparison =
                subscriptionPlanService.comparePlans(currentPlanId, targetPlanId);

        return ResponseEntity.ok(comparison);
    }

    @Override
    public ResponseEntity<Boolean> planHasFeature(UUID planId, String featureCode) {
        log.info("GET /api/v1/subscription-plans/{}/has-feature/{}", planId, featureCode);

        boolean hasFeature = subscriptionPlanService.planHasFeature(planId, featureCode);

        return ResponseEntity.ok(hasFeature);
    }

    @Override
    public ResponseEntity<SubscriptionPlanResponse.PlanStatsResponse> getPlanStats(UUID planId) {
        log.info("GET /api/v1/subscription-plans/{}/stats", planId);

        SubscriptionPlanResponse.PlanStatsResponse stats = subscriptionPlanService.getPlanStats(planId);

        return ResponseEntity.ok(stats);
    }
}