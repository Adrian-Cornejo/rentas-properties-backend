package com.rentas.properties.config;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Implementación de AuditorAware para capturar el usuario actual
 * en las operaciones de auditoría JPA.
 *
 * Se usa para llenar automáticamente los campos:
 * - @CreatedBy (created_by)
 * - @LastModifiedBy (cuando se implemente)
 *
 * Por ahora retorna un UUID fijo hasta que se implemente Spring Security con JWT.
 */
@Component
public class AuditorAwareImpl implements AuditorAware<UUID> {

    // UUID del usuario admin por defecto (del script SQL de inicialización)
    // Este es temporal hasta implementar Spring Security
    private static final UUID DEFAULT_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Override
    public Optional<UUID> getCurrentAuditor() {
        // Intenta obtener el usuario autenticado del SecurityContext
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Si no hay autenticación o es usuario anónimo, retorna el usuario por defecto
        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return Optional.of(DEFAULT_USER_ID);
        }

        // TODO: Cuando implementes Spring Security con JWT, descomentar esto:
        /*
        try {
            // Asume que el principal es un UserDetails personalizado con getId()
            if (authentication.getPrincipal() instanceof UserDetailsImpl userDetails) {
                return Optional.of(userDetails.getId());
            }
        } catch (Exception e) {
            // Si hay algún error, usa el usuario por defecto
            return Optional.of(DEFAULT_USER_ID);
        }
        */

        // Por ahora, siempre retorna el usuario por defecto
        return Optional.of(DEFAULT_USER_ID);
    }
}