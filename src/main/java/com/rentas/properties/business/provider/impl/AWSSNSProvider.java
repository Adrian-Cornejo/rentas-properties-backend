package com.rentas.properties.business.provider.impl;

import com.rentas.properties.api.exception.NotificationProviderException;
import com.rentas.properties.business.provider.NotificationProvider;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.MessageAttributeValue;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;

import java.util.HashMap;
import java.util.Map;

@Component("awsSNSProvider")
@Slf4j
public class AWSSNSProvider implements NotificationProvider {

    @Value("${aws.access.key.id:}")
    private String accessKeyId;

    @Value("${aws.secret.access.key:}")
    private String secretAccessKey;

    @Value("${aws.region:us-east-1}")
    private String region;

    private SnsClient snsClient;
    private boolean configured = false;

    @PostConstruct
    public void init() {
        if (accessKeyId != null && !accessKeyId.isEmpty() &&
                secretAccessKey != null && !secretAccessKey.isEmpty()) {

            try {
                AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKeyId, secretAccessKey);

                snsClient = SnsClient.builder()
                        .region(Region.of(region))
                        .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                        .build();

                configured = true;
                log.info("AWS SNS provider configurado correctamente para región: {}", region);
            } catch (Exception e) {
                log.error("Error al inicializar AWS SNS: {}", e.getMessage());
                configured = false;
            }
        } else {
            log.warn("AWS SNS provider no configurado - faltan credenciales");
            configured = false;
        }
    }

    @PreDestroy
    public void cleanup() {
        if (snsClient != null) {
            snsClient.close();
            log.info("AWS SNS client cerrado");
        }
    }

    @Override
    public String sendSMS(String phoneNumber, String message) {
        if (!configured) {
            throw new NotificationProviderException("AWS SNS provider no está configurado");
        }

        try {
            log.info("Enviando SMS a {} vía AWS SNS", phoneNumber);

            Map<String, MessageAttributeValue> smsAttributes = new HashMap<>();
            smsAttributes.put("AWS.SNS.SMS.SenderID", MessageAttributeValue.builder()
                    .stringValue("ArriendaFacil")
                    .dataType("String")
                    .build());
            smsAttributes.put("AWS.SNS.SMS.SMSType", MessageAttributeValue.builder()
                    .stringValue("Transactional")
                    .dataType("String")
                    .build());

            PublishRequest publishRequest = PublishRequest.builder()
                    .phoneNumber(phoneNumber)
                    .message(message)
                    .messageAttributes(smsAttributes)
                    .build();

            PublishResponse response = snsClient.publish(publishRequest);

            log.info("SMS enviado exitosamente. MessageId: {}", response.messageId());
            return response.messageId();

        } catch (Exception e) {
            log.error("Error al enviar SMS vía AWS SNS a {}: {}", phoneNumber, e.getMessage());
            throw new NotificationProviderException("Error al enviar SMS: " + e.getMessage(), e);
        }
    }

    @Override
    public String sendWhatsApp(String phoneNumber, String message) {
        throw new NotificationProviderException("AWS SNS provider solo soporta SMS. Use sendSMS()");
    }

    @Override
    public String getProviderName() {
        return "AWS_SNS";
    }

    @Override
    public boolean isConfigured() {
        return configured;
    }
}