package com.rentas.properties.config;

import com.rentas.properties.dao.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Implementación de AuditorAware para capturar el usuario actual
 * en las operaciones de auditoría JPA.
 *
 * Se usa para llenar automáticamente los campos:
 * - @CreatedBy (created_by)
 * - @LastModifiedBy (updated_by)
 *
 * Obtiene el UUID del usuario autenticado desde el SecurityContext.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuditorAwareImpl implements AuditorAware<UUID> {

    private final UserRepository userRepository;

    @Override
    public Optional<UUID> getCurrentAuditor() {
        try {
            // Obtener autenticación del SecurityContext
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            // Si no hay autenticación o es anónimo, retornar empty
            if (authentication == null
                    || !authentication.isAuthenticated()
                    || "anonymousUser".equals(authentication.getPrincipal())) {
                log.debug("No hay usuario autenticado - createdBy será NULL");
                return Optional.empty();
            }

            Object principal = authentication.getPrincipal();

            // Si el principal es UserDetails (Spring Security estándar)
            if (principal instanceof UserDetails userDetails) {
                String email = userDetails.getUsername();
                log.debug("Buscando UUID para usuario: {}", email);

                // Buscar el usuario en la BD por email
                return userRepository.findByEmail(email)
                        .map(user -> {
                            log.debug("Usuario encontrado - ID: {} | Email: {}", user.getId(), email);
                            return user.getId();
                        })
                        .or(() -> {
                            log.warn("Usuario autenticado '{}' no encontrado en BD", email);
                            return Optional.empty();
                        });
            }

            // Si el principal es un String (caso raro, pero posible)
            if (principal instanceof String email) {
                log.debug("Principal es String: {}", email);
                return userRepository.findByEmail(email)
                        .map(user -> user.getId());
            }

            log.warn("Principal desconocido: {}", principal.getClass().getName());
            return Optional.empty();

        } catch (Exception e) {
            log.error("Error al obtener auditor actual", e);
            return Optional.empty();
        }
    }
}