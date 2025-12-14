package com.rentas.properties.api.controller.impl;

import com.rentas.properties.api.controller.NotificationController;
import com.rentas.properties.api.dto.request.NotificationSettingsRequest;
import com.rentas.properties.api.dto.request.SendTestNotificationRequest;
import com.rentas.properties.api.dto.response.NotificationSettingsResponse;
import com.rentas.properties.api.dto.response.NotificationStatsResponse;
import com.rentas.properties.business.services.impl.NotificationServiceImpl;
import com.rentas.properties.dao.entity.Notification;
import com.rentas.properties.dao.repository.NotificationRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationControllerImpl implements NotificationController {

    private final NotificationServiceImpl notificationService;
    private final NotificationRepository notificationRepository;

    @Override
    @GetMapping("/settings")
    public ResponseEntity<NotificationSettingsResponse> getNotificationSettings() {
        log.info("Solicitando configuración de notificaciones");
        return ResponseEntity.ok(notificationService.getSettings());
    }

    @Override
    @PutMapping("/settings")
    public ResponseEntity<NotificationSettingsResponse> updateNotificationSettings(
            @Valid @RequestBody NotificationSettingsRequest request) {
        log.info("Actualizando configuración de notificaciones: {}", request);
        return ResponseEntity.ok(notificationService.updateSettings(request));
    }

    @Override
    @PostMapping("/test")
    public ResponseEntity<Map<String, String>> sendTestNotification(
            @Valid @RequestBody SendTestNotificationRequest request) {
        log.info("Enviando notificación de prueba a {}", request.getPhoneNumber());

        notificationService.sendTestNotification(request);

        return ResponseEntity.ok(Map.of(
                "message", "Notificación de prueba enviada exitosamente",
                "phoneNumber", request.getPhoneNumber(),
                "channel", request.getChannel()
        ));
    }

    @Override
    @GetMapping("/stats")
    public ResponseEntity<NotificationStatsResponse> getNotificationStats() {
        log.info("Solicitando estadísticas de notificaciones");
        return ResponseEntity.ok(notificationService.getStats());
    }

    @Override
    @PostMapping("/webhooks/twilio")
    public ResponseEntity<Void> twilioWebhook(@RequestParam Map<String, String> params) {
        log.info("Webhook de Twilio recibido: {}", params);

        try {
            String messageSid = params.get("MessageSid");
            String messageStatus = params.get("MessageStatus");

            if (messageSid != null && messageStatus != null) {
                updateNotificationStatus(messageSid, messageStatus);
            }

        } catch (Exception e) {
            log.error("Error procesando webhook de Twilio: {}", e.getMessage(), e);
        }

        return ResponseEntity.ok().build();
    }

    @Override
    @PostMapping("/webhooks/aws")
    public ResponseEntity<Void> awsWebhook(@RequestBody Map<String, Object> payload) {
        log.info("Webhook de AWS SNS recibido: {}", payload);

        try {
            // AWS SNS envía notificaciones en formato JSON
            // El formato puede variar según el tipo de notificación
            String messageId = (String) payload.get("MessageId");
            String status = (String) payload.get("Status");

            if (messageId != null && status != null) {
                updateNotificationStatus(messageId, status);
            }

        } catch (Exception e) {
            log.error("Error procesando webhook de AWS: {}", e.getMessage(), e);
        }

        return ResponseEntity.ok().build();
    }

    private void updateNotificationStatus(String providerMessageId, String status) {
        log.debug("Actualizando estado de notificación: {} -> {}", providerMessageId, status);

        // Buscar notificación por provider_message_id
        notificationRepository.findAll().stream()
                .filter(n -> providerMessageId.equals(n.getProviderMessageId()))
                .findFirst()
                .ifPresent(notification -> {
                    String mappedStatus = mapProviderStatus(status);
                    notification.setStatus(mappedStatus);

                    if ("DELIVERED".equals(mappedStatus)) {
                        notification.setDeliveredAt(LocalDateTime.now());
                    } else if ("FAILED".equals(mappedStatus)) {
                        notification.setErrorMessage("Fallo reportado por proveedor: " + status);
                    }

                    notificationRepository.save(notification);
                    log.info("Estado de notificación {} actualizado a {}", notification.getId(), mappedStatus);
                });
    }

    private String mapProviderStatus(String providerStatus) {
        // Mapeo de estados de Twilio y AWS a nuestros estados internos
        return switch (providerStatus.toUpperCase()) {
            case "SENT", "QUEUED" -> "SENT";
            case "DELIVERED", "DELIVERY_CONFIRMED" -> "DELIVERED";
            case "FAILED", "UNDELIVERED" -> "FAILED";
            default -> "SENT";
        };
    }
}