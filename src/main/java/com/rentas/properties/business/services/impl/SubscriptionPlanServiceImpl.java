// src/main/java/com/rentas/properties/service/impl/SubscriptionPlanServiceImpl.java
package com.rentas.properties.business.services.impl;

import com.rentas.properties.api.dto.response.SubscriptionPlanResponse;
import com.rentas.properties.api.exception.ResourceNotFoundException;
import com.rentas.properties.dao.entity.SubscriptionPlan;
import com.rentas.properties.dao.repository.SubscriptionPlanRepository;
import com.rentas.properties.service.SubscriptionPlanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class SubscriptionPlanServiceImpl implements SubscriptionPlanService {

    private final SubscriptionPlanRepository subscriptionPlanRepository;

    @Override
    public List<SubscriptionPlanResponse> getAllActivePlans() {
        log.info("Obteniendo todos los planes activos");

        List<SubscriptionPlan> plans = subscriptionPlanRepository.findByIsActiveTrueOrderByDisplayOrderAsc();

        return plans.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<SubscriptionPlanResponse> getAllPlans() {
        log.info("Obteniendo todos los planes (activos e inactivos)");

        List<SubscriptionPlan> plans = subscriptionPlanRepository.findAllByOrderByDisplayOrderAsc();

        return plans.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public SubscriptionPlanResponse getPlanById(UUID planId) {
        log.info("Obteniendo plan por ID: {}", planId);

        SubscriptionPlan plan = subscriptionPlanRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan no encontrado con ID: " + planId));

        return convertToResponse(plan);
    }

    @Override
    public SubscriptionPlanResponse getPlanByCode(String planCode) {
        log.info("Obteniendo plan por código: {}", planCode);

        SubscriptionPlan plan = subscriptionPlanRepository.findByPlanCode(planCode)
                .orElseThrow(() -> new ResourceNotFoundException("Plan no encontrado con código: " + planCode));

        return convertToResponse(plan);
    }

    @Override
    public SubscriptionPlanResponse getPopularPlan() {
        log.info("Obteniendo plan más popular");

        SubscriptionPlan plan = subscriptionPlanRepository.findByIsPopularTrueAndIsActiveTrue()
                .orElseThrow(() -> new ResourceNotFoundException("No hay plan marcado como popular"));

        return convertToResponse(plan);
    }

    @Override
    public SubscriptionPlanResponse getFreePlan() {
        log.info("Obteniendo plan gratuito");

        SubscriptionPlan plan = subscriptionPlanRepository.findFreePlan()
                .orElseThrow(() -> new ResourceNotFoundException("No hay plan gratuito disponible"));

        return convertToResponse(plan);
    }

    @Override
    public SubscriptionPlanResponse.PlanComparisonResponse comparePlans(UUID currentPlanId, UUID targetPlanId) {
        log.info("Comparando planes: {} vs {}", currentPlanId, targetPlanId);

        SubscriptionPlan currentPlan = subscriptionPlanRepository.findById(currentPlanId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan actual no encontrado"));

        SubscriptionPlan targetPlan = subscriptionPlanRepository.findById(targetPlanId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan objetivo no encontrado"));

        return buildComparison(currentPlan, targetPlan);
    }

    @Override
    public boolean planHasFeature(UUID planId, String featureCode) {
        log.info("Verificando si plan {} tiene feature: {}", planId, featureCode);

        SubscriptionPlan plan = subscriptionPlanRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan no encontrado"));

        return checkFeature(plan, featureCode);
    }

    @Override
    public SubscriptionPlanResponse.PlanStatsResponse getPlanStats(UUID planId) {
        log.info("Obteniendo estadísticas del plan: {}", planId);

        SubscriptionPlan plan = subscriptionPlanRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan no encontrado"));

        Long orgCount = subscriptionPlanRepository.countOrganizationsByPlan(planId);
        Long totalOrgs = subscriptionPlanRepository.findAll().stream()
                .mapToLong(p -> subscriptionPlanRepository.countOrganizationsByPlan(p.getId()))
                .sum();

        BigDecimal marketShare = totalOrgs > 0
                ? BigDecimal.valueOf(orgCount).multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(totalOrgs), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal monthlyRevenue = plan.getMonthlyPrice().multiply(BigDecimal.valueOf(orgCount));
        BigDecimal annualRevenue = plan.getAnnualPrice() != null
                ? plan.getAnnualPrice().multiply(BigDecimal.valueOf(orgCount))
                : monthlyRevenue.multiply(BigDecimal.valueOf(12));

        return SubscriptionPlanResponse.PlanStatsResponse.builder()
                .planId(plan.getId().toString())
                .planCode(plan.getPlanCode())
                .planName(plan.getPlanName())
                .organizationCount(orgCount)
                .marketShare(marketShare)
                .projectedMonthlyRevenue(monthlyRevenue)
                .projectedAnnualRevenue(annualRevenue)
                .build();
    }

    // ============================================
    // MÉTODOS PRIVADOS
    // ============================================

    private SubscriptionPlanResponse convertToResponse(SubscriptionPlan plan) {
        // Calcular ahorro anual
        BigDecimal annualSavings = null;
        if (plan.getAnnualPrice() != null && plan.getMonthlyPrice().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal monthlyTotal = plan.getMonthlyPrice().multiply(BigDecimal.valueOf(12));
            BigDecimal savings = monthlyTotal.subtract(plan.getAnnualPrice());
            annualSavings = savings.divide(monthlyTotal, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }

        return SubscriptionPlanResponse.builder()
                .id(plan.getId().toString())
                .planCode(plan.getPlanCode())
                .planName(plan.getPlanName())
                .planDescription(plan.getPlanDescription())
                // Pricing
                .monthlyPrice(plan.getMonthlyPrice())
                .annualPrice(plan.getAnnualPrice())
                .currency(plan.getCurrency())
                .trialDays(plan.getTrialDays())
                // Límites
                .maxProperties(plan.getMaxProperties())
                .maxUsers(plan.getMaxUsers())
                .maxActiveContracts(plan.getMaxActiveContracts())
                .storageLimitMb(plan.getStorageLimitMb())
                .imagesPerProperty(plan.getImagesPerProperty())
                .reportHistoryDays(plan.getReportHistoryDays())
                // Notificaciones
                .hasNotifications(plan.getHasNotifications())
                .notificationChannels(plan.getNotificationChannels())
                .monthlyNotificationLimit(plan.getMonthlyNotificationLimit())
                .hasLateReminders(plan.getHasLateReminders())
                .hasAdminDigest(plan.getHasAdminDigest())
                // Mantenimiento
                .hasMaintenanceScheduling(plan.getHasMaintenanceScheduling())
                .hasMaintenancePhotos(plan.getHasMaintenancePhotos())
                // Reportes
                .hasAdvancedReports(plan.getHasAdvancedReports())
                .hasDataExport(plan.getHasDataExport())
                .hasPdfReports(plan.getHasPdfReports())
                // Funcionalidades avanzadas
                .hasApiAccess(plan.getHasApiAccess())
                .hasWhiteLabel(plan.getHasWhiteLabel())
                .whiteLabelLevel(plan.getWhiteLabelLevel())
                .hasMultiCurrency(plan.getHasMultiCurrency())
                .hasDocumentManagement(plan.getHasDocumentManagement())
                .hasESignature(plan.getHasESignature())
                .hasTenantPortal(plan.getHasTenantPortal())
                .hasMobileApp(plan.getHasMobileApp())
                .hasIntegrations(plan.getHasIntegrations())
                // Soporte
                .supportLevel(plan.getSupportLevel())
                .supportResponseHours(plan.getSupportResponseHours())
                .hasOnboarding(plan.getHasOnboarding())
                .hasAccountManager(plan.getHasAccountManager())
                // Display
                .displayOrder(plan.getDisplayOrder())
                .isPopular(plan.getIsPopular())
                .isCustom(plan.getIsCustom())
                // Computed
                .unlimitedUsers(plan.isUnlimitedUsers())
                .unlimitedNotifications(plan.isUnlimitedNotifications())
                .unlimitedHistory(plan.isUnlimitedHistory())
                .allowsImages(plan.allowsImages())
                .annualSavingsPercentage(annualSavings)
                .build();
    }

    private SubscriptionPlanResponse.PlanComparisonResponse buildComparison(
            SubscriptionPlan current, SubscriptionPlan target) {

        boolean isUpgrade = target.getMonthlyPrice().compareTo(current.getMonthlyPrice()) > 0;

        BigDecimal priceDiff = target.getMonthlyPrice().subtract(current.getMonthlyPrice());
        BigDecimal priceDiffPercent = current.getMonthlyPrice().compareTo(BigDecimal.ZERO) > 0
                ? priceDiff.divide(current.getMonthlyPrice(), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;

        List<String> improvements = new ArrayList<>();
        List<String> downgrades = new ArrayList<>();

        // Comparar límites
        if (target.getMaxProperties() > current.getMaxProperties()) {
            improvements.add(String.format("Propiedades: %d → %d",
                    current.getMaxProperties(), target.getMaxProperties()));
        } else if (target.getMaxProperties() < current.getMaxProperties()) {
            downgrades.add(String.format("Propiedades: %d → %d",
                    current.getMaxProperties(), target.getMaxProperties()));
        }

        if (target.getMaxUsers() > current.getMaxUsers() || target.isUnlimitedUsers()) {
            improvements.add(String.format("Usuarios: %d → %s",
                    current.getMaxUsers(),
                    target.isUnlimitedUsers() ? "Ilimitados" : target.getMaxUsers()));
        }

        if (target.getImagesPerProperty() > current.getImagesPerProperty()) {
            improvements.add(String.format("Imágenes por propiedad: %d → %d",
                    current.getImagesPerProperty(), target.getImagesPerProperty()));
        }

        // Comparar funcionalidades
        if (target.getHasNotifications() && !current.getHasNotifications()) {
            improvements.add("Notificaciones automáticas activadas");
        }

        if (target.getHasMaintenancePhotos() && !current.getHasMaintenancePhotos()) {
            improvements.add("Fotos en mantenimiento habilitadas");
        }

        if (target.getHasApiAccess() && !current.getHasApiAccess()) {
            improvements.add("Acceso a API habilitado");
        }

        if (target.getHasWhiteLabel() && !current.getHasWhiteLabel()) {
            improvements.add("White-label habilitado");
        }

        return SubscriptionPlanResponse.PlanComparisonResponse.builder()
                .currentPlan(convertToResponse(current))
                .targetPlan(convertToResponse(target))
                .isUpgrade(isUpgrade)
                .priceDifference(priceDiff)
                .priceDifferencePercentage(priceDiffPercent)
                .improvements(improvements)
                .downgrades(downgrades)
                .build();
    }

    private boolean checkFeature(SubscriptionPlan plan, String featureCode) {
        return switch (featureCode.toUpperCase()) {
            case "NOTIFICATIONS" -> plan.getHasNotifications();
            case "MAINTENANCE_PHOTOS" -> plan.getHasMaintenancePhotos();
            case "ADVANCED_REPORTS" -> plan.getHasAdvancedReports();
            case "DATA_EXPORT" -> plan.getHasDataExport();
            case "PDF_REPORTS" -> plan.getHasPdfReports();
            case "API_ACCESS" -> plan.getHasApiAccess();
            case "WHITE_LABEL" -> plan.getHasWhiteLabel();
            case "MULTI_CURRENCY" -> plan.getHasMultiCurrency();
            case "E_SIGNATURE" -> plan.getHasESignature();
            case "TENANT_PORTAL" -> plan.getHasTenantPortal();
            case "MOBILE_APP" -> plan.getHasMobileApp();
            case "INTEGRATIONS" -> plan.getHasIntegrations();
            default -> false;
        };
    }
}