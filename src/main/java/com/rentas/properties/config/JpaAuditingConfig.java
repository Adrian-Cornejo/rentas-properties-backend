package com.rentas.properties.config;

import com.rentas.properties.dao.repository.UserRepository;
import lombok.RequiredArgsConstructor;
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
 * - @LastModifiedBy (updated_by)
 *
 * Esta configuración habilita la auditoría automática en entidades
 * que usan @EntityListeners(AuditingEntityListener.class)
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
@RequiredArgsConstructor
public class JpaAuditingConfig {

    private final UserRepository userRepository;

    /**
     * Bean que proporciona el usuario actual para auditoría
     * Se usa en campos @CreatedBy y @LastModifiedBy
     *
     * @return AuditorAware configurado con UserRepository
     */
    @Bean
    public AuditorAware<UUID> auditorProvider() {
        return new AuditorAwareImpl(userRepository);
    }
}