package com.rentas.properties.business.scheduler;

import com.rentas.properties.business.services.impl.NotificationServiceImpl;
import com.rentas.properties.dao.repository.NotificationRepository;
import com.rentas.properties.dao.repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationScheduler {

    private final NotificationServiceImpl notificationService;
    private final OrganizationRepository organizationRepository;
    private final NotificationRepository notificationRepository;

    /**
     * Job que se ejecuta diariamente a las 8:00 AM hora México
     * Procesa todos los recordatorios de pago del día
     */
    @Scheduled(cron = "${notification.job.cron:0 0 8 * * ?}") // 8:00 AM todos los días
    @Transactional
    public void processDailyPaymentReminders() {
        log.info("========== INICIO: Proceso de recordatorios diarios ==========");

        try {
            // Primero resetear contadores mensuales si es día 1
            resetMonthlyCountersIfNeeded();

            // Procesar recordatorios
            notificationService.processDailyReminders();

            log.info("========== FIN: Proceso de recordatorios diarios completado ==========");

        } catch (Exception e) {
            log.error("========== ERROR: Proceso de recordatorios diarios falló: {} ==========",
                    e.getMessage(), e);
        }
    }

    /**
     * Job que se ejecuta cada hora para reintentar notificaciones pendientes
     */
    @Scheduled(fixedDelay = 3600000) // Cada 1 hora
    @Transactional
    public void retryPendingNotifications() {
        log.debug("Verificando notificaciones pendientes para reintento");

        try {
            LocalDateTime threshold = LocalDateTime.now().minusHours(24);
            int maxRetries = Integer.parseInt(System.getProperty("notification.retry.max", "3"));

            var pendingNotifications = notificationRepository.findPendingNotificationsOlderThan(
                    threshold, maxRetries
            );

            if (!pendingNotifications.isEmpty()) {
                log.info("Se encontraron {} notificaciones pendientes para reintento",
                        pendingNotifications.size());

                for (var notification : pendingNotifications) {
                    if (notification.getRetryCount() >= maxRetries) {
                        notification.setStatus("FAILED");
                        notification.setErrorMessage("Excedido número máximo de reintentos");
                        notificationRepository.save(notification);
                        log.warn("Notificación {} marcada como FAILED después de {} reintentos",
                                notification.getId(), maxRetries);
                    }
                }
            }

        } catch (Exception e) {
            log.error("Error en proceso de reintento de notificaciones: {}", e.getMessage(), e);
        }
    }

    private void resetMonthlyCountersIfNeeded() {
        LocalDate today = LocalDate.now();

        if (today.getDayOfMonth() == 1) {
            log.info("Primer día del mes - Reseteando contadores de notificaciones");

            try {
                organizationRepository.resetMonthlyNotificationCounters(today);
                log.info("Contadores mensuales reseteados exitosamente");
            } catch (Exception e) {
                log.error("Error reseteando contadores mensuales: {}", e.getMessage(), e);
            }
        }
    }
}