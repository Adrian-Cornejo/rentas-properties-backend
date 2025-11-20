package com.rentas.properties.config;

import com.rentas.properties.dao.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
@Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
@RequiredArgsConstructor
public class AuditorAwareImpl implements AuditorAware<UUID> {

    private final UserRepository userRepository;

    // Cache para evitar múltiples consultas en la misma request
    private UUID cachedAuditorId;
    private boolean alreadyLookedUp = false;

    @Override
    public Optional<UUID> getCurrentAuditor() {
        // Si ya buscamos el auditor en esta request, retornar el cache
        if (alreadyLookedUp) {
            return Optional.ofNullable(cachedAuditorId);
        }

        alreadyLookedUp = true;

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null
                    || !authentication.isAuthenticated()
                    || "anonymousUser".equals(authentication.getPrincipal())) {
                log.debug("No hay usuario autenticado - createdBy será NULL");
                return Optional.empty();
            }

            Object principal = authentication.getPrincipal();

            // Si el principal es UserDetails
            if (principal instanceof UserDetails userDetails) {
                String email = userDetails.getUsername();
                log.debug("Buscando UUID para usuario: {}", email);

                cachedAuditorId = userRepository.findByEmail(email)
                        .map(user -> {
                            log.debug("Usuario encontrado - ID: {} | Email: {}", user.getId(), email);
                            return user.getId();
                        })
                        .orElseGet(() -> {
                            log.warn("Usuario autenticado '{}' no encontrado en BD", email);
                            return null;
                        });

                return Optional.ofNullable(cachedAuditorId);
            }

            // Si el principal es un String
            if (principal instanceof String email) {
                log.debug("Principal es String: {}", email);
                cachedAuditorId = userRepository.findByEmail(email)
                        .map(user -> user.getId())
                        .orElse(null);

                return Optional.ofNullable(cachedAuditorId);
            }

            log.warn("Principal desconocido: {}", principal.getClass().getName());
            return Optional.empty();

        } catch (Exception e) {
            log.error("Error al obtener auditor actual", e);
            return Optional.empty();
        }
    }
}