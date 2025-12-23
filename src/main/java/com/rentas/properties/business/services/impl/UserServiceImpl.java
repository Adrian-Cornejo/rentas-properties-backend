package com.rentas.properties.business.services.impl;

import com.rentas.properties.api.dto.request.JoinOrganizationRequest;
import com.rentas.properties.api.dto.request.UpdateUserRequest;
import com.rentas.properties.api.dto.response.UserDetailResponse;
import com.rentas.properties.api.dto.response.UserResponse;
import com.rentas.properties.api.exception.*;
import com.rentas.properties.business.services.UserService;
import com.rentas.properties.dao.entity.Organization;
import com.rentas.properties.dao.entity.User;
import com.rentas.properties.dao.repository.OrganizationRepository;
import com.rentas.properties.dao.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers(boolean includeInactive) {
        log.info("Obteniendo todos los usuarios - includeInactive: {}", includeInactive);

        User currentUser = getCurrentUser();

        if ("ADMIN".equals(currentUser.getRole())) {
            List<User> users;
            if (includeInactive) {
                users = userRepository.findAll();
            } else {
                users = userRepository.findByIsActiveTrue();
            }
            log.debug("Usuario ADMIN - Retornando {} usuarios", users.size());
            return users.stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        }

        if (currentUser.getOrganization() != null) {
            List<User> users = userRepository.findByOrganization_Id(currentUser.getOrganization().getId());
            log.debug("Usuario regular - Retornando {} usuarios de su organización", users.size());
            return users.stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        }

        log.warn("Usuario sin organización: {}", currentUser.getId());
        return List.of();
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetailResponse getUserById(UUID id) {
        log.info("Obteniendo usuario con ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado con ID: " + id));

        User currentUser = getCurrentUser();
        validateUserCanAccessUser(currentUser, user);

        return mapToDetailResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetailResponse getCurrentUserProfile() {
        log.info("Obteniendo perfil del usuario actual");

        User currentUser = getCurrentUser();
        return mapToDetailResponse(currentUser);
    }

    @Override
    @Transactional
    public UserDetailResponse updateUser(UUID id, UpdateUserRequest request) {
        log.info("Actualizando usuario con ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado con ID: " + id));

        User currentUser = getCurrentUser();
        validateUserCanModifyUser(currentUser, user);

        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }

        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }

        if ("ADMIN".equals(currentUser.getRole())) {
            if (request.getRole() != null) {
                validateRole(request.getRole());
                user.setRole(request.getRole().toUpperCase());
            }

            if (request.getAccountStatus() != null) {
                validateAccountStatus(request.getAccountStatus());
                user.setAccountStatus(request.getAccountStatus());
            }
        }

        User updatedUser = userRepository.save(user);
        log.info("Usuario actualizado exitosamente: {}", updatedUser.getEmail());

        return mapToDetailResponse(updatedUser);
    }

    @Override
    @Transactional
    public void deactivateUser(UUID id) {
        log.info("Desactivando usuario con ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado con ID: " + id));

        User currentUser = getCurrentUser();
        validateUserIsAdmin(currentUser);

        if (user.getId().equals(currentUser.getId())) {
            log.warn("Usuario intentó desactivarse a sí mismo: {}", currentUser.getEmail());
            throw new UnauthorizedAccessException("No puedes desactivarte a ti mismo");
        }

        user.setIsActive(false);
        user.setAccountStatus("suspended");

        if (user.getOrganization() != null) {
            Organization organization = user.getOrganization();
            organization.decrementUsersCount();
            organizationRepository.save(organization);
            log.info("Contador de usuarios decrementado para organización {}: {}/{}",
                    organization.getId(), organization.getCurrentUsersCount(), organization.getSubscriptionPlan().getMaxUsers());
        }

        userRepository.save(user);

        log.info("Usuario desactivado exitosamente: {}", id);
    }

    @Override
    @Transactional
    public void activateUser(UUID id) {
        log.info("Activando usuario con ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado con ID: " + id));

        User currentUser = getCurrentUser();
        validateUserIsAdmin(currentUser);

        if (user.getOrganization() != null) {
            Organization organization = user.getOrganization();
            if (!organization.canAddUser()) {
                log.warn("No se puede activar usuario. Organización {} alcanzó límite. Plan: {}, Actual: {}, Máximo: {}",
                        organization.getId(),
                        organization.getPlanCode(),
                        organization.getCurrentUsersCount(),
                        organization.getSubscriptionPlan().getMaxUsers());
                throw new OrganizationUserLimitException(
                        "No se puede activar el usuario. La organización ha alcanzado el límite máximo de usuarios (" +
                                organization.getSubscriptionPlan().getMaxUsers() + ") según su plan " + organization.getPlanCode() + ". " +
                                "Por favor, mejore el plan para agregar más usuarios."
                );
            }

            organization.incrementUsersCount();
            organizationRepository.save(organization);
            log.info("Contador de usuarios incrementado para organización {}: {}/{}",
                    organization.getId(), organization.getCurrentUsersCount(), organization.getSubscriptionPlan().getMaxUsers());
        }

        user.setIsActive(true);
        user.setAccountStatus("active");
        userRepository.save(user);

        log.info("Usuario activado exitosamente: {}", id);
    }

    @Override
    @Transactional
    public UserDetailResponse joinOrganization(JoinOrganizationRequest request) {
        log.info("Usuario intentando unirse a organización con código: {}", request.getInvitationCode());

        User currentUser = getCurrentUser();

        if (currentUser.getOrganization() != null) {
            log.warn("Usuario {} ya pertenece a una organización", currentUser.getEmail());
            throw new UserAlreadyHasOrganizationException(
                    "Ya perteneces a una organización. No puedes cambiar de organización."
            );
        }

        Organization organization = organizationRepository.findByInvitationCode(request.getInvitationCode())
                .orElseThrow(() -> new InvitationCodeInvalidException(
                        "Código de invitación inválido: " + request.getInvitationCode()
                ));

        if (!organization.getIsActive()) {
            log.warn("Intento de unirse a organización inactiva: {}", organization.getId());
            throw new OrganizationNotActiveException("La organización no está activa");
        }

        if (!organization.canAddUser()) {
            log.warn("Organización {} alcanzó límite de usuarios. Plan: {}, Actual: {}, Máximo: {}",
                    organization.getId(),
                    organization.getPlanCode(),
                    organization.getCurrentUsersCount(),
                    organization.getSubscriptionPlan().getMaxUsers());
            throw new OrganizationUserLimitException(
                    "La organización alcanzó el límite máximo de usuarios (" + organization.getSubscriptionPlan().getMaxUsers() + ") " +
                            "según su plan " + organization.getPlanCode() + ". " +
                            "Por favor, solicita al administrador que mejore el plan."
            );
        }

        currentUser.setOrganization(organization);
        currentUser.setOrganizationJoinedAt(LocalDateTime.now());
        currentUser.setAccountStatus("active");

        organization.incrementUsersCount();

        userRepository.save(currentUser);
        organizationRepository.save(organization);

        log.info("Usuario {} unido exitosamente a organización {}. Contador: {}/{}",
                currentUser.getEmail(), organization.getName(),
                organization.getCurrentUsersCount(), organization.getSubscriptionPlan().getMaxUsers());

        return mapToDetailResponse(currentUser);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getUsersByOrganization() {
        log.info("Obteniendo usuarios de la organización");

        User currentUser = getCurrentUser();

        if (currentUser.getOrganization() == null) {
            log.warn("Usuario sin organización: {}", currentUser.getId());
            throw new UnauthorizedAccessException("Debes pertenecer a una organización");
        }

        List<User> users = userRepository.findByOrganization_Id(currentUser.getOrganization().getId());

        log.debug("Se encontraron {} usuarios en la organización", users.size());

        return users.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getUsersWithoutOrganization() {
        log.info("Obteniendo usuarios sin organización");

        User currentUser = getCurrentUser();
        validateUserIsAdmin(currentUser);

        List<User> users = userRepository.findUsersWithoutOrganization();

        log.debug("Se encontraron {} usuarios sin organización", users.size());

        return users.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getUsersByAccountStatus(String status) {
        log.info("Obteniendo usuarios por estado: {}", status);

        validateAccountStatus(status);

        User currentUser = getCurrentUser();

        List<User> users;
        if ("ADMIN".equals(currentUser.getRole())) {
            users = userRepository.findByAccountStatus(status);
        } else if (currentUser.getOrganization() != null) {
            users = userRepository.findByOrganization_IdAndAccountStatus(
                    currentUser.getOrganization().getId(), status
            );
        } else {
            return List.of();
        }

        log.debug("Se encontraron {} usuarios con estado {}", users.size(), status);

        return users.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
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

    private void validateUserCanAccessUser(User currentUser, User targetUser) {
        if ("ADMIN".equals(currentUser.getRole())) {
            return;
        }

        if (currentUser.getId().equals(targetUser.getId())) {
            return;
        }

        if (currentUser.getOrganization() != null && targetUser.getOrganization() != null
                && currentUser.getOrganization().getId().equals(targetUser.getOrganization().getId())) {
            return;
        }

        log.warn("Usuario {} intentó acceder a usuario {} sin permisos",
                currentUser.getEmail(), targetUser.getId());
        throw new UnauthorizedAccessException("No tienes acceso a este usuario");
    }

    private void validateUserCanModifyUser(User currentUser, User targetUser) {
        if ("ADMIN".equals(currentUser.getRole())) {
            return;
        }

        if (currentUser.getId().equals(targetUser.getId())) {
            return;
        }

        log.warn("Usuario {} intentó modificar usuario {} sin permisos",
                currentUser.getEmail(), targetUser.getId());
        throw new UnauthorizedAccessException("Solo puedes modificar tu propio perfil");
    }

    private void validateRole(String role) {
        if (!"USER".equals(role.toUpperCase()) && !"ADMIN".equals(role.toUpperCase())) {
            throw new IllegalArgumentException("Rol inválido. Debe ser USER o ADMIN");
        }
    }

    private void validateAccountStatus(String status) {
        if (!"pending".equals(status) && !"active".equals(status) && !"suspended".equals(status)) {
            throw new IllegalArgumentException("Estado inválido. Debe ser: pending, active o suspended");
        }
    }

    private UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .role(user.getRole())
                .isActive(user.getIsActive())
                .accountStatus(user.getAccountStatus())
                .organizationId(user.getOrganization() != null ? user.getOrganization().getId() : null)
                .organizationName(user.getOrganization() != null ? user.getOrganization().getName() : null)
                .lastLogin(user.getLastLogin())
                .createdAt(user.getCreatedAt())
                .build();
    }

    private UserDetailResponse mapToDetailResponse(User user) {
        UserDetailResponse.OrganizationDto organizationDto = null;
        if (user.getOrganization() != null) {
            Organization org = user.getOrganization();
            organizationDto = UserDetailResponse.OrganizationDto.builder()
                    .id(org.getId())
                    .name(org.getName())
                    .invitationCode(org.getInvitationCode())
                    .subscriptionPlan(org.getPlanCode())
                    .subscriptionStatus(org.getSubscriptionStatus())
                    .build();
        }

        return UserDetailResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .role(user.getRole())
                .isActive(user.getIsActive())
                .accountStatus(user.getAccountStatus())
                .organization(organizationDto)
                .organizationJoinedAt(user.getOrganizationJoinedAt())
                .lastLogin(user.getLastLogin())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}