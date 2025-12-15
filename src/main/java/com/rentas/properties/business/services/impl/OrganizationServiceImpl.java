package com.rentas.properties.business.services.impl;

import com.rentas.properties.api.dto.request.CreateOrganizationRequest;
import com.rentas.properties.api.dto.request.UpdateOrganizationRequest;
import com.rentas.properties.api.dto.response.OrganizationDetailResponse;
import com.rentas.properties.api.dto.response.OrganizationInfoResponse;
import com.rentas.properties.api.dto.response.OrganizationResponse;
import com.rentas.properties.api.dto.response.OrganizationStatsResponse;
import com.rentas.properties.api.exception.InvitationCodeAlreadyExistsException;
import com.rentas.properties.api.exception.InvitationCodeInvalidException;
import com.rentas.properties.api.exception.OrganizationNotFoundException;
import com.rentas.properties.api.exception.UnauthorizedAccessException;
import com.rentas.properties.business.services.CloudinaryService;
import com.rentas.properties.business.services.OrganizationService;
import com.rentas.properties.dao.entity.Organization;
import com.rentas.properties.dao.entity.User;
import com.rentas.properties.dao.repository.OrganizationRepository;
import com.rentas.properties.dao.repository.PropertyRepository;
import com.rentas.properties.dao.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrganizationServiceImpl implements OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final PropertyRepository propertyRepository;
    private final CloudinaryService cloudinaryService;

    @Override
    @Transactional
    public OrganizationDetailResponse createOrganization(CreateOrganizationRequest request) {
        log.info("Creando organización: {}", request.getName());

        User currentUser = getCurrentUser();
        validateUserIsAdmin(currentUser);

        String invitationCode = generateUniqueInvitationCode();
        log.debug("Código de invitación generado: {}", invitationCode);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime trialEndsAt = now.plusDays(30);

        Organization organization = Organization.builder()
                .name(request.getName())
                .description(request.getDescription())
                .primaryColor(request.getPrimaryColor() != null ? request.getPrimaryColor() : "#3B82F6")
                .secondaryColor(request.getSecondaryColor() != null ? request.getSecondaryColor() : "#10B981")
                .invitationCode(invitationCode)
                .logoUrl(request.getLogoUrl())
                .codeIsReusable(true)
                .owner(currentUser)
                .maxUsers(3)
                .maxProperties(5)
                .currentUsersCount(1)
                .currentPropertiesCount(0)
                .subscriptionStatus("TRIAL")
                .trialEndsAt(trialEndsAt)
                .isActive(true)
                .createdAt(now)
                .updatedAt(now)
                .build();

        Organization savedOrganization = organizationRepository.save(organization);
        log.info("Organización creada exitosamente con ID: {}", savedOrganization.getId());

        currentUser.setOrganization(savedOrganization);
        currentUser.setOrganizationJoinedAt(now);
        currentUser.setAccountStatus("ACTIVE");
        currentUser.setUpdatedAt(now);
        userRepository.save(currentUser);

        log.info("Usuario {} asignado como OWNER de la organización {}",
                currentUser.getEmail(), savedOrganization.getId());

        return mapToDetailResponse(savedOrganization);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrganizationResponse> getAllOrganizations() {
        log.info("Obteniendo todas las organizaciones");

        User currentUser = getCurrentUser();

        if ("ADMIN".equals(currentUser.getRole())) {
            List<Organization> organizations = organizationRepository.findAll();
            log.debug("Usuario ADMIN - Retornando {} organizaciones", organizations.size());
            return organizations.stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        }

        if (currentUser.getOrganization() != null) {
            Organization userOrganization = currentUser.getOrganization();
            log.debug("Usuario regular - Retornando solo su organización: {}", userOrganization.getId());
            return List.of(mapToResponse(userOrganization));
        }

        log.warn("Usuario sin organización asignada: {}", currentUser.getId());
        return List.of();
    }

    @Override
    @Transactional(readOnly = true)
    public OrganizationDetailResponse getOrganizationById(UUID id) {
        log.info("Obteniendo organización con ID: {}", id);

        Organization organization = organizationRepository.findById(id)
                .orElseThrow(() -> new OrganizationNotFoundException("Organización no encontrada con ID: " + id));

        User currentUser = getCurrentUser();
        validateUserCanAccessOrganization(currentUser, organization);

        return mapToDetailResponse(organization);
    }

    @Override
    @Transactional
    public OrganizationDetailResponse updateOrganization(UUID id, UpdateOrganizationRequest request) {
        log.info("Actualizando organización con ID: {}", id);

        Organization organization = organizationRepository.findById(id)
                .orElseThrow(() -> new OrganizationNotFoundException("Organización no encontrada con ID: " + id));

        User currentUser = getCurrentUser();
        validateUserCanModifyOrganization(currentUser, organization);

        String oldLogoUrl = organization.getLogoUrl();
        String newLogoUrl = request.getLogoUrl();

        if (newLogoUrl != null && !newLogoUrl.equals(oldLogoUrl)) {
            deleteOldLogoIfExists(oldLogoUrl);
        }

        if ((newLogoUrl == null || newLogoUrl.isEmpty()) && oldLogoUrl != null) {
            deleteOldLogoIfExists(oldLogoUrl);
        }

        if (request.getName() != null) {
            organization.setName(request.getName());
        }

        if (request.getDescription() != null) {
            organization.setDescription(request.getDescription());
        }

        if (request.getPrimaryColor() != null) {
            organization.setPrimaryColor(request.getPrimaryColor());
        }

        if (request.getSecondaryColor() != null) {
            organization.setSecondaryColor(request.getSecondaryColor());
        }

        if (request.getLogoUrl() != null) {
            organization.setLogoUrl(request.getLogoUrl());
        }

        if (request.getLogoPublicId() != null) {
            organization.setLogoPublicId(request.getLogoPublicId());
        }

        if (request.getCodeIsReusable() != null) {
            organization.setCodeIsReusable(request.getCodeIsReusable());
        }

        if ("ADMIN".equals(currentUser.getRole())) {
            if (request.getMaxUsers() != null) {
                organization.setMaxUsers(request.getMaxUsers());
            }

            if (request.getMaxProperties() != null) {
                organization.setMaxProperties(request.getMaxProperties());
            }

            if (request.getSubscriptionStatus() != null) {
                organization.setSubscriptionStatus(request.getSubscriptionStatus());
            }

        }

        Organization updatedOrganization = organizationRepository.save(organization);
        log.info("Organización actualizada exitosamente: {}", updatedOrganization.getName());

        return mapToDetailResponse(updatedOrganization);
    }

    @Override
    @Transactional
    public void deleteOrganization(UUID id) {
        log.info("Eliminando organización con ID: {}", id);

        Organization organization = organizationRepository.findById(id)
                .orElseThrow(() -> new OrganizationNotFoundException("Organización no encontrada con ID: " + id));

        User currentUser = getCurrentUser();
        validateUserIsAdmin(currentUser);

        organization.setIsActive(false);
        organizationRepository.save(organization);

        log.info("Organización desactivada exitosamente: {}", id);
    }

    @Override
    @Transactional
    public OrganizationDetailResponse regenerateInvitationCode(UUID id) {
        log.info("Regenerando código de invitación para organización: {}", id);

        Organization organization = organizationRepository.findById(id)
                .orElseThrow(() -> new OrganizationNotFoundException("Organización no encontrada con ID: " + id));

        User currentUser = getCurrentUser();
        validateUserCanModifyOrganization(currentUser, organization);

        String newCode = generateUniqueInvitationCode();
        organization.setInvitationCode(newCode);

        Organization updatedOrganization = organizationRepository.save(organization);
        log.info("Código de invitación regenerado: {}", newCode);

        return mapToDetailResponse(updatedOrganization);
    }

    @Override
    @Transactional(readOnly = true)
    public OrganizationResponse validateInvitationCode(String code) {
        log.info("Validando código de invitación: {}", code);

        Organization organization = organizationRepository.findByInvitationCode(code)
                .orElseThrow(() -> new InvitationCodeInvalidException("Código de invitación inválido: " + code));

        if (!organization.getIsActive()) {
            log.warn("Código válido pero organización inactiva: {}", code);
            throw new InvitationCodeInvalidException("La organización asociada a este código está inactiva");
        }

        if ("suspended".equals(organization.getSubscriptionStatus()) ||
                "cancelled".equals(organization.getSubscriptionStatus())) {
            log.warn("Código válido pero suscripción suspendida/cancelada: {}", code);
            throw new InvitationCodeInvalidException("La organización no puede aceptar nuevos miembros en este momento");
        }

        if (organization.getCurrentUsersCount() >= organization.getMaxUsers()) {
            log.warn("Código válido pero límite de usuarios alcanzado: {}", code);
            throw new InvitationCodeInvalidException("La organización ha alcanzado el límite máximo de usuarios");
        }

        log.info("Código de invitación válido para organización: {}", organization.getName());
        return mapToResponse(organization);
    }

    @Override
    @Transactional(readOnly = true)
    public OrganizationStatsResponse getOrganizationStats(UUID id) {
        log.info("Obteniendo estadísticas de organización: {}", id);

        Organization organization = organizationRepository.findById(id)
                .orElseThrow(() -> new OrganizationNotFoundException("Organización no encontrada con ID: " + id));

        User currentUser = getCurrentUser();
        validateUserCanAccessOrganization(currentUser, organization);

        Long activeUsers = userRepository.countByOrganizationId(id);
        Long activeProperties = propertyRepository.countActiveByOrganization_Id(id);

        return OrganizationStatsResponse.builder()
                .organizationId(organization.getId())
                .organizationName(organization.getName())
                .currentUsersCount(organization.getCurrentUsersCount())
                .maxUsers(organization.getMaxUsers())
                .usersAvailable(organization.getMaxUsers() - organization.getCurrentUsersCount())
                .usersPercentage(calculatePercentage(organization.getCurrentUsersCount(), organization.getMaxUsers()))
                .currentPropertiesCount(organization.getCurrentPropertiesCount())
                .maxProperties(organization.getMaxProperties())
                .propertiesAvailable(organization.getMaxProperties() - organization.getCurrentPropertiesCount())
                .propertiesPercentage(calculatePercentage(organization.getCurrentPropertiesCount(), organization.getMaxProperties()))
                .subscriptionStatus(organization.getSubscriptionStatus())
                .trialEndsAt(organization.getTrialEndsAt())
                .subscriptionEndsAt(organization.getSubscriptionEndsAt())
                .isActive(organization.getIsActive())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrganizationResponse> getActiveOrganizations() {
        log.info("Obteniendo organizaciones activas");

        List<Organization> organizations = organizationRepository.findByIsActiveTrue();
        log.debug("Se encontraron {} organizaciones activas", organizations.size());

        return organizations.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<OrganizationDetailResponse> getMyOrganization() {
        log.info("Obteniendo organización del usuario autenticado");

        User currentUser = getCurrentUser();

        if (currentUser.getOrganization() == null) {
            log.warn("Usuario {} no tiene organización asignada", currentUser.getEmail());
            return Optional.empty();
        }

        Organization organization = currentUser.getOrganization();
        log.debug("Organización encontrada: {} para usuario: {}", organization.getName(), currentUser.getEmail());

        return Optional.of(mapToDetailResponse(organization));
    }

    @Override
    @Transactional(readOnly = true)
    public OrganizationStatsResponse getMyOrganizationStats() {
        log.info("Obteniendo estadísticas de organización del usuario autenticado");

        User currentUser = getCurrentUser();

        if (currentUser.getOrganization() == null) {
            log.warn("Usuario {} no tiene organización asignada", currentUser.getEmail());
            throw new OrganizationNotFoundException("No tienes una organización asignada");
        }

        Organization organization = currentUser.getOrganization();

        return getOrganizationStats(organization.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public OrganizationInfoResponse getMyOrganizationInfo() {
        log.info("Obteniendo información básica de organización del usuario autenticado");

        User currentUser = getCurrentUser();

        if (currentUser.getOrganization() == null) {
            log.warn("Usuario {} no tiene organización asignada", currentUser.getEmail());
            throw new OrganizationNotFoundException("No tienes una organización asignada");
        }

        Organization organization = currentUser.getOrganization();
        log.debug("Organización encontrada: {} para usuario: {}", organization.getName(), currentUser.getEmail());

        return OrganizationInfoResponse.builder()
                .id(organization.getId())
                .name(organization.getName())
                .logoUrl(organization.getLogoUrl())
                .subscriptionStatus(organization.getSubscriptionStatus())
                .maxProperties(organization.getMaxProperties())
                .currentPropertiesCount(organization.getCurrentPropertiesCount())
                .maxUsers(organization.getMaxUsers())
                .currentUsersCount(organization.getCurrentUsersCount())
                .primaryColor(organization.getPrimaryColor())
                .secondaryColor(organization.getSecondaryColor())
                .build();
    }

    private User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email;

        if (principal instanceof UserDetails) {
            email = ((UserDetails) principal).getUsername();
        } else {
            email = principal.toString();
        }

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedAccessException("Usuario no autenticado"));
    }

    private void validateUserIsAdmin(User user) {
        if (!"ADMIN".equals(user.getRole())) {
            log.warn("Usuario {} intentó realizar acción de ADMIN sin permisos", user.getEmail());
            throw new UnauthorizedAccessException("Solo los administradores pueden realizar esta acción");
        }
    }

    private void validateUserCanAccessOrganization(User user, Organization organization) {
        if ("ADMIN".equals(user.getRole())) {
            return;
        }

        if (user.getOrganization() == null || !user.getOrganization().getId().equals(organization.getId())) {
            log.warn("Usuario {} intentó acceder a organización {} sin permisos", user.getEmail(), organization.getId());
            throw new UnauthorizedAccessException("No tienes acceso a esta organización");
        }
    }

    private void validateUserCanModifyOrganization(User user, Organization organization) {
        if ("ADMIN".equals(user.getRole())) {
            return;
        }

        if (organization.getOwner() == null || !organization.getOwner().getId().equals(user.getId())) {
            log.warn("Usuario {} intentó modificar organización {} sin ser owner", user.getEmail(), organization.getId());
            throw new UnauthorizedAccessException("Solo el propietario de la organización puede modificarla");
        }
    }

    private String generateUniqueInvitationCode() {
        String code;
        int attempts = 0;
        int maxAttempts = 10;

        do {
            code = generateInvitationCode();
            attempts++;

            if (attempts >= maxAttempts) {
                log.error("No se pudo generar un código único después de {} intentos", maxAttempts);
                throw new InvitationCodeAlreadyExistsException("No se pudo generar un código de invitación único");
            }
        } while (organizationRepository.existsByInvitationCode(code));

        return code;
    }

    private String generateInvitationCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuilder code = new StringBuilder();

        for (int i = 0; i < 3; i++) {
            code.append(chars.charAt(random.nextInt(26)));
        }

        code.append("-");

        for (int i = 0; i < 2; i++) {
            code.append(chars.charAt(random.nextInt(10) + 26));
        }

        code.append(chars.charAt(random.nextInt(26)));
        code.append(chars.charAt(random.nextInt(10) + 26));

        return code.toString();
    }

    private double calculatePercentage(Integer current, Integer max) {
        if (max == null || max == 0) {
            return 0.0;
        }
        return (current * 100.0) / max;
    }

    private OrganizationResponse mapToResponse(Organization organization) {
        return OrganizationResponse.builder()
                .id(organization.getId())
                .name(organization.getName())
                .description(organization.getDescription())
                .logoUrl(organization.getLogoUrl())
                .primaryColor(organization.getPrimaryColor())
                .secondaryColor(organization.getSecondaryColor())
                .invitationCode(organization.getInvitationCode())
                .maxUsers(organization.getMaxUsers())
                .maxProperties(organization.getMaxProperties())
                .currentUsersCount(organization.getCurrentUsersCount())
                .currentPropertiesCount(organization.getCurrentPropertiesCount())
                .subscriptionStatus(organization.getSubscriptionStatus())
                .isActive(organization.getIsActive())
                .createdAt(organization.getCreatedAt())
                .build();
    }

    private OrganizationDetailResponse mapToDetailResponse(Organization organization) {
        OrganizationDetailResponse.OwnerDto ownerDto = null;
        if (organization.getOwner() != null) {
            User owner = organization.getOwner();
            ownerDto = OrganizationDetailResponse.OwnerDto.builder()
                    .id(owner.getId())
                    .email(owner.getEmail())
                    .fullName(owner.getFullName())
                    .phone(owner.getPhone())
                    .build();
        }

        return OrganizationDetailResponse.builder()
                .id(organization.getId())
                .name(organization.getName())
                .description(organization.getDescription())
                .logoUrl(organization.getLogoUrl())
                .logoPublicId(organization.getLogoPublicId())
                .primaryColor(organization.getPrimaryColor())
                .secondaryColor(organization.getSecondaryColor())
                .invitationCode(organization.getInvitationCode())
                .codeIsReusable(organization.getCodeIsReusable())
                .owner(ownerDto)
                .maxUsers(organization.getMaxUsers())
                .maxProperties(organization.getMaxProperties())
                .currentUsersCount(organization.getCurrentUsersCount())
                .currentPropertiesCount(organization.getCurrentPropertiesCount())
                .subscriptionStatus(organization.getSubscriptionStatus())
                .trialEndsAt(organization.getTrialEndsAt())
                .subscriptionStartedAt(organization.getSubscriptionStartedAt())
                .subscriptionEndsAt(organization.getSubscriptionEndsAt())
                .isActive(organization.getIsActive())
                .createdAt(organization.getCreatedAt())
                .updatedAt(organization.getUpdatedAt())
                .build();
    }

    private void deleteOldLogoIfExists(String oldLogoUrl) {
        if (oldLogoUrl != null && !oldLogoUrl.isEmpty()) {
            try {
                String publicId = cloudinaryService.extractPublicIdFromUrl(oldLogoUrl);
                if (publicId != null) {
                    cloudinaryService.deleteImage(publicId);
                    log.info("Deleted old logo from Cloudinary - publicId: {}", publicId);
                }
            } catch (Exception e) {
                log.warn("Failed to delete old logo from Cloudinary: {}", e.getMessage());
            }
        }
    }
}