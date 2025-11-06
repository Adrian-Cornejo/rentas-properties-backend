package com.rentas.properties.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.UUID;

/**
 * Configuración de JPA Auditing para campos automáticos:
 * - @CreatedDate (created_at)
 * - @LastModifiedDate (updated_at)
 * - @CreatedBy (created_by)
 *
 * Esta configuración habilita la auditoría automática en entidades
 * que usan @EntityListeners(AuditingEntityListener.class)
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class JpaAuditingConfig {

    /**
     * Bean que proporciona el usuario actual para auditoría
     * Se usa en campos @CreatedBy y @LastModifiedBy
     */
    @Bean
    public AuditorAware<UUID> auditorProvider() {
        return new AuditorAwareImpl();
    }
}