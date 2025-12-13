package com.rentas.properties.business.provider;

/**
 * Interface para proveedores de notificaciones (SMS y WhatsApp).
 * Permite migrar entre proveedores sin cambiar la lógica de negocio.
 */
public interface NotificationProvider {

    /**
     * Envía un mensaje SMS
     *
     * @param phoneNumber Teléfono en formato +52XXXXXXXXXX
     * @param message Contenido del mensaje
     * @return ID del mensaje en el proveedor
     * @throws com.rentas.properties.api.exception.NotificationProviderException si falla el envío
     */
    String sendSMS(String phoneNumber, String message);

    /**
     * Envía un mensaje por WhatsApp
     *
     * @param phoneNumber Teléfono en formato +52XXXXXXXXXX
     * @param message Contenido del mensaje
     * @return ID del mensaje en el proveedor
     * @throws com.rentas.properties.api.exception.NotificationProviderException si falla el envío
     */
    String sendWhatsApp(String phoneNumber, String message);

    /**
     * Obtiene el nombre del proveedor
     *
     * @return Nombre del proveedor (ej: "TWILIO", "AWS_SNS")
     */
    String getProviderName();

    /**
     * Verifica si el proveedor está configurado correctamente
     *
     * @return true si está configurado, false en caso contrario
     */
    boolean isConfigured();
}