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

@Component("twilioSMSProvider")
@Slf4j
public class TwilioSMSProvider implements NotificationProvider {

    @Value("${twilio.account.sid:}")
    private String accountSid;

    @Value("${twilio.auth.token:}")
    private String authToken;

    @Value("${twilio.sms.number:}")
    private String smsNumber;

    private boolean configured = false;

    @PostConstruct
    public void init() {
        if (accountSid != null && !accountSid.isEmpty() &&
                authToken != null && !authToken.isEmpty() &&
                smsNumber != null && !smsNumber.isEmpty()) {

            try {
                // Twilio ya fue inicializado por TwilioWhatsAppProvider
                // Solo verificamos que tengamos el número SMS
                configured = true;
                log.info("Twilio SMS provider configurado correctamente con número: {}", smsNumber);
            } catch (Exception e) {
                log.error("Error al inicializar Twilio SMS: {}", e.getMessage());
                configured = false;
            }
        } else {
            log.warn("Twilio SMS provider no configurado - faltan credenciales o número SMS");
            configured = false;
        }
    }

    @Override
    public String sendSMS(String phoneNumber, String message) {
        if (!configured) {
            throw new NotificationProviderException("Twilio SMS provider no está configurado");
        }

        try {
            log.info("Enviando SMS a {} vía Twilio", phoneNumber);

            Message twilioMessage = Message.creator(
                    new PhoneNumber(phoneNumber),
                    new PhoneNumber(smsNumber),
                    message
            ).create();

            log.info("SMS enviado exitosamente. SID: {}", twilioMessage.getSid());
            return twilioMessage.getSid();

        } catch (Exception e) {
            log.error("Error al enviar SMS vía Twilio a {}: {}", phoneNumber, e.getMessage());
            throw new NotificationProviderException("Error al enviar SMS: " + e.getMessage(), e);
        }
    }

    @Override
    public String sendWhatsApp(String phoneNumber, String message) {
        throw new NotificationProviderException("Twilio SMS provider solo soporta SMS. Use TwilioWhatsAppProvider para WhatsApp");
    }

    @Override
    public String getProviderName() {
        return "TWILIO_SMS";
    }

    @Override
    public boolean isConfigured() {
        return configured;
    }
}