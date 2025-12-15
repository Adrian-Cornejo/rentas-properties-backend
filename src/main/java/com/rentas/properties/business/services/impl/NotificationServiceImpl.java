package com.rentas.properties.business.services.impl;

import com.rentas.properties.api.dto.request.NotificationSettingsRequest;
import com.rentas.properties.api.dto.request.SendTestNotificationRequest;
import com.rentas.properties.api.dto.response.NotificationSettingsResponse;
import com.rentas.properties.api.dto.response.NotificationStatsResponse;
import com.rentas.properties.api.exception.*;
import com.rentas.properties.business.provider.NotificationProvider;
import com.rentas.properties.dao.entity.*;
import com.rentas.properties.dao.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl {

    private final NotificationRepository notificationRepository;
    private final OrganizationRepository organizationRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final ContractRepository contractRepository;

    @Qualifier("twilioWhatsAppProvider")
    private final NotificationProvider twilioWhatsAppProvider;

    @Qualifier("twilioSMSProvider")
    private final NotificationProvider twilioSMSProvider;

    @Qualifier("awsSNSProvider")
    private final NotificationProvider awsSNSProvider;

    @Value("${notification.sms.provider:AWS_SNS}")
    private String smsProviderName; // AWS_SNS o TWILIO

    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 5000;

    @Transactional
    public void processDailyReminders() {
        log.info("Iniciando proceso de recordatorios diarios");

        LocalDate today = LocalDate.now();
        LocalDate threeDaysLater = today.plusDays(3);
        LocalDate threeDaysAgo = today.minusDays(3);

        // Obtener organizaciones con notificaciones habilitadas
        List<Organization> organizations = organizationRepository.findOrganizationsWithNotificationsEnabled();

        log.info("Procesando {} organizaciones con notificaciones habilitadas", organizations.size());

        for (Organization org : organizations) {
            try {
                processOrganizationReminders(org, today, threeDaysLater, threeDaysAgo);
            } catch (Exception e) {
                log.error("Error procesando notificaciones para organización {}: {}",
                        org.getId(), e.getMessage(), e);
            }
        }

        log.info("Proceso de recordatorios diarios finalizado");
    }

    @Transactional
    public void processOrganizationReminders(Organization org, LocalDate today,
                                             LocalDate threeDaysLater, LocalDate threeDaysAgo) {

        log.info("Procesando recordatorios para organización: {} ({})", org.getName(), org.getId());

        // Validar límite mensual
        if (org.getNotificationsSentThisMonth() >= org.getNotificationLimit()) {
            log.warn("Organización {} ha excedido su límite mensual de notificaciones", org.getId());
            // TODO: Enviar email al admin
            return;
        }

        String channel = org.getNotificationChannels();
        SubscriptionPlan plan = org.getSubscriptionPlan();

        // Obtener pagos que requieren notificación
        List<Payment> paymentsToNotify = new ArrayList<>();

        // 3 días antes del vencimiento
        List<Payment> paymentsDueIn3Days = paymentRepository.findByDueDateAndOrganization(
                threeDaysLater, org.getId()
        );
        paymentsToNotify.addAll(filterNotifiablePayments(paymentsDueIn3Days));

        // Día del vencimiento
        List<Payment> paymentsDueToday = paymentRepository.findByDueDateAndOrganization(
                today, org.getId()
        );
        paymentsToNotify.addAll(filterNotifiablePayments(paymentsDueToday));

        // 3 días después (solo SUPERIOR y si está atrasado)
        if ("SUPERIOR".equals(plan)) {
            List<Payment> paymentsOverdue3Days = paymentRepository.findByDueDateAndOrganization(
                            threeDaysAgo, org.getId()
                    ).stream()
                    .filter(p -> "ATRASADO".equals(p.getStatus()))
                    .collect(Collectors.toList());
            paymentsToNotify.addAll(paymentsOverdue3Days);
        }

        log.info("Se encontraron {} pagos para notificar en organización {}",
                paymentsToNotify.size(), org.getId());

        int sentCount = 0;

        for (Payment payment : paymentsToNotify) {
            // Validar que no exceda límite
            if (org.getNotificationsSentThisMonth() + sentCount >= org.getNotificationLimit()) {
                log.warn("Se alcanzó el límite de notificaciones para organización {}", org.getId());
                break;
            }

            try {
                sendPaymentReminder(payment, org, channel);
                sentCount++;
            } catch (Exception e) {
                log.error("Error enviando notificación para pago {}: {}", payment.getId(), e.getMessage());
            }
        }

        // Actualizar contador
        if (sentCount > 0) {
            organizationRepository.incrementNotificationCount(org.getId(), sentCount);
        }

        // Enviar consolidado al admin si está habilitado
        if (org.getAdminNotifications() && !paymentsToNotify.isEmpty()) {
            sendAdminConsolidatedReport(org, paymentsToNotify, channel);
        }
    }

    private List<Payment> filterNotifiablePayments(List<Payment> payments) {
        return payments.stream()
                .filter(p -> "PENDIENTE".equals(p.getStatus()) || "ATRASADO".equals(p.getStatus()))
                .filter(p -> !"ADELANTO".equals(p.getPaymentType()))
                .collect(Collectors.toList());
    }

    @Transactional
    public void sendPaymentReminder(Payment payment, Organization org, String channel) {
        log.info("Enviando recordatorio para pago {}", payment.getId());

        Contract contract = payment.getContract();

        // Obtener inquilino PRIMARY
        ContractTenant primaryTenant = contract.getContractTenants().stream()
                .filter(ContractTenant::getIsPrimary)
                .findFirst()
                .orElse(null);

        if (primaryTenant == null || primaryTenant.getTenant() == null) {
            log.warn("No se encontró inquilino PRIMARY para contrato {}", contract.getId());
            return;
        }

        Tenant tenant = primaryTenant.getTenant();
        String phone = normalizePhoneNumber(tenant.getPhone());

        if (phone == null) {
            log.warn("Inquilino {} no tiene teléfono válido", tenant.getId());
            return;
        }

        String message = buildPaymentReminderMessage(payment, tenant, contract);
        String title = "Recordatorio de pago";

        // Determinar días hasta vencimiento
        long daysUntilDue = ChronoUnit.DAYS.between(LocalDate.now(), payment.getDueDate());

        if (daysUntilDue < 0) {
            title = "Pago atrasado";
        } else if (daysUntilDue == 0) {
            title = "Pago vence hoy";
        } else {
            title = "Recordatorio de pago";
        }

        // Enviar según canal
        if ("BOTH".equals(channel)) {
            sendNotificationWithRetry(phone, message, "SMS", payment, tenant, title);
            sendNotificationWithRetry(phone, message, "WHATSAPP", payment, tenant, title);
        } else if ("SMS".equals(channel)) {
            sendNotificationWithRetry(phone, message, "SMS", payment, tenant, title);
        } else if ("WHATSAPP".equals(channel)) {
            sendNotificationWithRetry(phone, message, "WHATSAPP", payment, tenant, title);
        }
    }

    private void sendNotificationWithRetry(String phone, String message, String channel,
                                           Payment payment, Tenant tenant, String title) {

        Notification notification = Notification.builder()
                .recipientType("TENANT")
                .recipientId(tenant.getId())
                .recipientPhone(phone)
                .notificationType("PAYMENT_REMINDER")
                .title(title)
                .message(message)
                .channel(channel)
                .status("PENDING")
                .relatedPayment(payment)
                .relatedContract(payment.getContract())
                .retryCount(0)
                .build();

        notification = notificationRepository.save(notification);

        int retries = 0;
        boolean sent = false;

        while (retries < MAX_RETRIES && !sent) {
            try {
                String messageId = sendNotification(phone, message, channel);

                notification.setProviderMessageId(messageId);
                notification.setStatus("SENT");
                notification.setSentAt(LocalDateTime.now());
                notificationRepository.save(notification);

                log.info("Notificación enviada exitosamente por {} a {}", channel, phone);
                sent = true;

            } catch (Exception e) {
                retries++;
                notification.setRetryCount(retries);
                notification.setLastRetryAt(LocalDateTime.now());

                if (retries >= MAX_RETRIES) {
                    notification.setStatus("FAILED");
                    notification.setErrorMessage(e.getMessage());
                    log.error("Notificación fallida después de {} reintentos: {}",
                            MAX_RETRIES, e.getMessage());
                } else {
                    log.warn("Reintento {}/{} para notificación {}", retries, MAX_RETRIES,
                            notification.getId());
                    try {
                        Thread.sleep(RETRY_DELAY_MS * retries); // Backoff exponencial
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }

                notificationRepository.save(notification);
            }
        }
    }

    private String sendNotification(String phone, String message, String channel) {
        if ("SMS".equals(channel)) {
            return sendSMSWithSelectedProvider(phone, message);
        } else if ("WHATSAPP".equals(channel)) {
            if (!twilioWhatsAppProvider.isConfigured()) {
                throw new NotificationProviderException("Twilio WhatsApp no está configurado");
            }
            return twilioWhatsAppProvider.sendWhatsApp(phone, message);
        } else {
            throw new IllegalArgumentException("Canal no soportado: " + channel);
        }
    }

    /**
     * Envía SMS usando el proveedor configurado (Twilio o AWS SNS)
     */
    private String sendSMSWithSelectedProvider(String phone, String message) {
        NotificationProvider smsProvider;

        if ("TWILIO".equalsIgnoreCase(smsProviderName)) {
            if (!twilioSMSProvider.isConfigured()) {
                log.warn("Twilio SMS no configurado, intentando con AWS SNS como fallback");
                smsProvider = awsSNSProvider;
            } else {
                smsProvider = twilioSMSProvider;
            }
        } else {
            // Default: AWS_SNS
            if (!awsSNSProvider.isConfigured()) {
                log.warn("AWS SNS no configurado, intentando con Twilio SMS como fallback");
                smsProvider = twilioSMSProvider;
            } else {
                smsProvider = awsSNSProvider;
            }
        }

        if (!smsProvider.isConfigured()) {
            throw new NotificationProviderException(
                    "Ningún proveedor de SMS está configurado. Configura Twilio o AWS SNS");
        }

        log.info("Enviando SMS usando proveedor: {}", smsProvider.getProviderName());
        return smsProvider.sendSMS(phone, message);
    }

    private String buildPaymentReminderMessage(Payment payment, Tenant tenant, Contract contract) {
        long daysUntilDue = ChronoUnit.DAYS.between(LocalDate.now(), payment.getDueDate());

        if (daysUntilDue == 3) {
            return String.format(
                    "Hola %s,\n\n" +
                            "Tu pago de renta vence en 3 días (%s).\n" +
                            "Monto: $%.2f\n" +
                            "Propiedad: %s",
                    tenant.getFullName(),
                    payment.getDueDate(),
                    payment.getTotalAmount(),
                    contract.getProperty().getAddress()
            );
        } else if (daysUntilDue == 0) {
            return String.format(
                    "Hola %s,\n\n" +
                            "Tu pago de renta vence HOY (%s).\n" +
                            "Monto: $%.2f\n" +
                            "Por favor realiza tu pago a la brevedad.",
                    tenant.getFullName(),
                    payment.getDueDate(),
                    payment.getTotalAmount()
            );
        } else { // Atrasado
            return String.format(
                    "Hola %s,\n\n" +
                            "Tu pago de renta está atrasado por %d días.\n" +
                            "Monto pendiente: $%.2f\n" +
                            "Por favor contacta a tu administrador.",
                    tenant.getFullName(),
                    Math.abs(daysUntilDue),
                    payment.getTotalAmount()
            );
        }
    }

    private void sendAdminConsolidatedReport(Organization org, List<Payment> payments, String channel) {
        log.info("Enviando reporte consolidado al admin de organización {}", org.getId());

        User owner = org.getOwner();
        if (owner == null || owner.getPhone() == null) {
            log.warn("Owner de organización {} no tiene teléfono", org.getId());
            return;
        }

        String phone = normalizePhoneNumber(owner.getPhone());
        if (phone == null) {
            return;
        }

        long paymentsDueToday = payments.stream()
                .filter(p -> p.getDueDate().isEqual(LocalDate.now()))
                .count();

        long paymentsOverdue = payments.stream()
                .filter(p -> "ATRASADO".equals(p.getStatus()))
                .count();

        double totalDueToday = payments.stream()
                .filter(p -> p.getDueDate().isEqual(LocalDate.now()))
                .mapToDouble(p -> p.getTotalAmount().doubleValue())
                .sum();

        double totalOverdue = payments.stream()
                .filter(p -> "ATRASADO".equals(p.getStatus()))
                .mapToDouble(p -> p.getTotalAmount().doubleValue())
                .sum();

        String message = String.format(
                "Resumen de pagos del día:\n\n" +
                        "- %d pagos vencen hoy: $%.2f\n" +
                        "- %d pagos atrasados: $%.2f\n" +
                        "Total: %d contratos requieren atención",
                paymentsDueToday,
                totalDueToday,
                paymentsOverdue,
                totalOverdue,
                paymentsDueToday + paymentsOverdue
        );

        try {
            sendNotification(phone, message, channel.equals("BOTH") ? "WHATSAPP" : channel);
            log.info("Reporte consolidado enviado al admin");
        } catch (Exception e) {
            log.error("Error enviando reporte consolidado: {}", e.getMessage());
        }
    }

    private String normalizePhoneNumber(String phone) {
        if (phone == null || phone.isEmpty()) {
            return null;
        }

        // Eliminar espacios, guiones, paréntesis
        phone = phone.replaceAll("[\\s\\-()]+", "");

        // Si ya tiene +52, validar formato
        if (phone.startsWith("+52")) {
            if (phone.matches("^\\+52[0-9]{10}$")) {
                return phone;
            } else {
                throw new InvalidPhoneNumberException("Formato inválido: " + phone);
            }
        }

        // Si empieza con 52, agregar +
        if (phone.startsWith("52") && phone.length() == 12) {
            return "+" + phone;
        }

        // Si son 10 dígitos, agregar +52
        if (phone.matches("^[0-9]{10}$")) {
            return "+52" + phone;
        }

        throw new InvalidPhoneNumberException("No se pudo normalizar el teléfono: " + phone);
    }

    @Transactional(readOnly = true)
    public NotificationSettingsResponse getSettings() {
        User currentUser = getCurrentUser();
        validateUserHasOrganization(currentUser);

        Organization org = currentUser.getOrganization();

        int remaining = Math.max(0, org.getNotificationLimit() - org.getNotificationsSentThisMonth());

        return NotificationSettingsResponse.builder()
                .enabled(org.getNotificationEnabled())
                .channel(org.getNotificationChannels())
                .adminNotifications(org.getAdminNotifications())
                .sentThisMonth(org.getNotificationsSentThisMonth())
                .monthlyLimit(org.getNotificationLimit())
                .remainingCredits(remaining)
                .subscriptionPlan(org.getPlanCode())
                .build();
    }

    @Transactional
    public NotificationSettingsResponse updateSettings(NotificationSettingsRequest request) {
        User currentUser = getCurrentUser();
        validateUserHasOrganization(currentUser);
        validateUserIsAdmin(currentUser);

        Organization org = currentUser.getOrganization();
        String plan = org.getPlanCode();

        // Validar según plan
        if ("BASICO".equals(plan) && request.getEnabled()) {
            throw new UnauthorizedAccessException(
                    "El plan BASICO no incluye notificaciones. Actualiza a plan INTERMEDIO o SUPERIOR");
        }

        if ("INTERMEDIO".equals(plan) && "BOTH".equals(request.getChannel())) {
            throw new UnauthorizedAccessException(
                    "El plan INTERMEDIO solo permite SMS o WHATSAPP, no ambos. Actualiza a plan SUPERIOR");
        }

        // Calcular límite según plan
        int limit = calculateNotificationLimit(org);

        org.setNotificationEnabled(request.getEnabled());
        org.setNotificationChannels(request.getChannel());
        org.setAdminNotifications(request.getAdminNotifications() != null ? request.getAdminNotifications() : true);
        org.setNotificationLimit(limit);

        if (org.getLastNotificationReset() == null) {
            org.setLastNotificationReset(LocalDate.now());
        }

        organizationRepository.save(org);

        log.info("Configuración de notificaciones actualizada para organización {}", org.getId());

        return getSettings();
    }

    private int calculateNotificationLimit(Organization org) {
        String plan = org.getPlanCode();

        if ("BASICO".equals(plan)) {
            return 0;
        } else if ("INTERMEDIO".equals(plan)) {
            int maxProperties = org.getMaxProperties();
            return maxProperties * 3 * 2; // propiedades × 3 contratos × 2 recordatorios
        } else if ("SUPERIOR".equals(plan)) {
            return 1000;
        }

        return 0;
    }

    @Transactional
    public void sendTestNotification(SendTestNotificationRequest request) {
        User currentUser = getCurrentUser();
        validateUserHasOrganization(currentUser);

        String phone = normalizePhoneNumber(request.getPhoneNumber());
        String channel = request.getChannel();

        String message = "Esta es una notificación de prueba de ArriendaFácil. " +
                "Tu sistema de notificaciones está configurado correctamente.";

        try {
            sendNotification(phone, message, channel);
            log.info("Notificación de prueba enviada a {}", phone);
        } catch (Exception e) {
            log.error("Error enviando notificación de prueba: {}", e.getMessage());
            throw new NotificationProviderException("Error al enviar notificación de prueba: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public NotificationStatsResponse getStats() {
        User currentUser = getCurrentUser();
        validateUserHasOrganization(currentUser);

        UUID orgId = currentUser.getOrganization().getId();

        // Obtener conteos por estado
        Map<String, Long> statusCounts = notificationRepository.countByStatusAndOrganization(orgId).stream()
                .collect(Collectors.toMap(
                        arr -> (String) arr[0],
                        arr -> (Long) arr[1]
                ));

        int totalSent = statusCounts.getOrDefault("SENT", 0L).intValue() +
                statusCounts.getOrDefault("DELIVERED", 0L).intValue();
        int totalDelivered = statusCounts.getOrDefault("DELIVERED", 0L).intValue();
        int totalFailed = statusCounts.getOrDefault("FAILED", 0L).intValue();

        Organization org = currentUser.getOrganization();

        int remaining = Math.max(0, org.getNotificationLimit() - org.getNotificationsSentThisMonth());

        // Calcular tasa de entrega
        var deliveryRate = NotificationStatsResponse.calculateDeliveryRate(totalDelivered, totalSent);

        // Obtener datos para gráfica (últimos 30 días)
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        List<Object[]> chartDataRaw = notificationRepository.getChartDataByOrganization(
                orgId, thirtyDaysAgo, LocalDateTime.now()
        );

        List<NotificationStatsResponse.ChartData> chartData = processChartData(chartDataRaw);

        // Obtener últimas 10 notificaciones
        List<Notification> recent = notificationRepository.findRecentByOrganization(orgId).stream()
                .limit(10)
                .collect(Collectors.toList());

        List<NotificationStatsResponse.RecentNotification> recentNotifications = recent.stream()
                .map(n -> NotificationStatsResponse.RecentNotification.builder()
                        .type(n.getNotificationType())
                        .channel(n.getChannel())
                        .status(n.getStatus())
                        .recipientPhone(maskPhoneNumber(n.getRecipientPhone()))
                        .sentAt(n.getSentAt() != null ? n.getSentAt().toString() : null)
                        .build())
                .collect(Collectors.toList());

        return NotificationStatsResponse.builder()
                .totalSent(totalSent)
                .totalDelivered(totalDelivered)
                .totalFailed(totalFailed)
                .sentThisMonth(org.getNotificationsSentThisMonth())
                .monthlyLimit(org.getNotificationLimit())
                .remainingCredits(remaining)
                .deliveryRate(deliveryRate)
                .chartData(chartData)
                .recentNotifications(recentNotifications)
                .build();
    }

    private List<NotificationStatsResponse.ChartData> processChartData(List<Object[]> raw) {
        Map<LocalDate, NotificationStatsResponse.ChartData> dataMap = new HashMap<>();

        for (Object[] row : raw) {
            LocalDate date = (LocalDate) row[0];
            String status = (String) row[1];
            Long count = (Long) row[2];

            NotificationStatsResponse.ChartData data = dataMap.computeIfAbsent(date,
                    d -> NotificationStatsResponse.ChartData.builder()
                            .date(d)
                            .sent(0)
                            .delivered(0)
                            .failed(0)
                            .build());

            if ("SENT".equals(status) || "DELIVERED".equals(status)) {
                data.setSent(data.getSent() + count.intValue());
            }
            if ("DELIVERED".equals(status)) {
                data.setDelivered(data.getDelivered() + count.intValue());
            }
            if ("FAILED".equals(status)) {
                data.setFailed(data.getFailed() + count.intValue());
            }
        }

        return dataMap.values().stream()
                .sorted(Comparator.comparing(NotificationStatsResponse.ChartData::getDate))
                .collect(Collectors.toList());
    }

    private String maskPhoneNumber(String phone) {
        if (phone == null || phone.length() < 8) {
            return phone;
        }
        return phone.substring(0, 6) + "XXXXX" + phone.substring(phone.length() - 2);
    }

    private User getCurrentUser() {
        String email = ((UserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal()).getUsername();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedAccessException("Usuario no autenticado"));
    }

    private void validateUserHasOrganization(User user) {
        if (user.getOrganization() == null) {
            throw new UnauthorizedAccessException(
                    "Debes pertenecer a una organización para realizar esta acción");
        }
    }

    private void validateUserIsAdmin(User user) {
        if (!"ADMIN".equals(user.getRole())) {
            throw new UnauthorizedAccessException(
                    "Solo usuarios ADMIN pueden modificar la configuración de notificaciones");
        }
    }
}