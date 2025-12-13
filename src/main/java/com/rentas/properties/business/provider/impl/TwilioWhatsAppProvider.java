package com.rentas.properties.business.provider.impl;

import com.rentas.properties.api.exception.NotificationProviderException;
import com.rentas.properties.business.provider.NotificationProvider;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Component("twilioWhatsAppProvider")
@Slf4j
public class TwilioWhatsAppProvider implements NotificationProvider {

    @Value("${twilio.account.sid:}")
    private String accountSid;

    @Value("${twilio.auth.token:}")
    private String authToken;

    @Value("${twilio.whatsapp.number:}")
    private String whatsappNumber;

    private boolean configured = false;

    @PostConstruct
    public void init() {
        if (accountSid != null && !accountSid.isEmpty() &&
                authToken != null && !authToken.isEmpty() &&
                whatsappNumber != null && !whatsappNumber.isEmpty()) {

            try {
                Twilio.init(accountSid, authToken);
                configured = true;
                log.info("Twilio WhatsApp provider configurado correctamente");
            } catch (Exception e) {
                log.error("Error al inicializar Twilio: {}", e.getMessage());
                configured = false;
            }
        } else {
            log.warn("Twilio WhatsApp provider no configurado - faltan credenciales");
            configured = false;
        }
    }

    @Override
    public String sendSMS(String phoneNumber, String message) {
        throw new NotificationProviderException("Twilio provider solo soporta WhatsApp. Use sendWhatsApp()");
    }

    @Override
    public String sendWhatsApp(String phoneNumber, String message) {
        if (!configured) {
            throw new NotificationProviderException("Twilio WhatsApp provider no está configurado");
        }

        try {
            log.info("Enviando WhatsApp a {} vía Twilio", phoneNumber);

            Message twilioMessage = Message.creator(
                    new PhoneNumber("whatsapp:" + phoneNumber),
                    new PhoneNumber("whatsapp:" + whatsappNumber),
                    message
            ).create();

            log.info("WhatsApp enviado exitosamente. SID: {}", twilioMessage.getSid());
            return twilioMessage.getSid();

        } catch (Exception e) {
            log.error("Error al enviar WhatsApp vía Twilio a {}: {}", phoneNumber, e.getMessage());
            throw new NotificationProviderException("Error al enviar WhatsApp: " + e.getMessage(), e);
        }
    }

    @Override
    public String getProviderName() {
        return "TWILIO";
    }

    @Override
    public boolean isConfigured() {
        return configured;
    }
}