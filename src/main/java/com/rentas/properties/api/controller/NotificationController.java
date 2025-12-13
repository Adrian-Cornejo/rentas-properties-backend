package com.rentas.properties.api.controller;

import com.rentas.properties.api.dto.request.NotificationSettingsRequest;
import com.rentas.properties.api.dto.request.SendTestNotificationRequest;
import com.rentas.properties.api.dto.response.NotificationSettingsResponse;
import com.rentas.properties.api.dto.response.NotificationStatsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@Tag(name = "Notifications", description = "Endpoints para gestión de notificaciones automáticas (SMS/WhatsApp)")
public interface NotificationController {

    @Operation(
            summary = "Obtener configuración de notificaciones",
            description = "Obtiene la configuración actual de notificaciones de la organización del usuario"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Configuración obtenida exitosamente",
                    content = @Content(schema = @Schema(implementation = NotificationSettingsResponse.class))
            ),
            @ApiResponse(responseCode = "403", description = "No tienes permisos")
    })
    ResponseEntity<NotificationSettingsResponse> getNotificationSettings();

    @Operation(
            summary = "Actualizar configuración de notificaciones",
            description = "Actualiza la configuración de notificaciones. Solo usuarios ADMIN pueden realizar esta acción."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Configuración actualizada exitosamente",
                    content = @Content(schema = @Schema(implementation = NotificationSettingsResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "403", description = "No tienes permisos")
    })
    ResponseEntity<NotificationSettingsResponse> updateNotificationSettings(
            @Valid @RequestBody NotificationSettingsRequest request
    );

    @Operation(
            summary = "Enviar notificación de prueba",
            description = "Envía una notificación de prueba al teléfono especificado"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notificación enviada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "503", description = "Proveedor no configurado o no disponible")
    })
    ResponseEntity<Map<String, String>> sendTestNotification(
            @Valid @RequestBody SendTestNotificationRequest request
    );

    @Operation(
            summary = "Obtener estadísticas de notificaciones",
            description = "Obtiene estadísticas y métricas de notificaciones de la organización"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Estadísticas obtenidas exitosamente",
                    content = @Content(schema = @Schema(implementation = NotificationStatsResponse.class))
            )
    })
    ResponseEntity<NotificationStatsResponse> getNotificationStats();

    @Operation(
            summary = "Webhook para Twilio",
            description = "Recibe actualizaciones de estado de mensajes desde Twilio WhatsApp"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Webhook procesado exitosamente")
    })
    ResponseEntity<Void> twilioWebhook(@RequestParam Map<String, String> params);

    @Operation(
            summary = "Webhook para AWS SNS",
            description = "Recibe actualizaciones de estado de mensajes desde AWS SNS"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Webhook procesado exitosamente")
    })
    ResponseEntity<Void> awsWebhook(@RequestBody Map<String, Object> payload);
}