package com.rentas.properties.business.services.impl;

import com.rentas.properties.api.dto.request.CreateTenantRequest;
import com.rentas.properties.api.dto.request.UpdateTenantRequest;
import com.rentas.properties.api.dto.response.TenantDetailResponse;
import com.rentas.properties.api.dto.response.TenantResponse;
import com.rentas.properties.api.exception.TenantAlreadyExistsException;
import com.rentas.properties.api.exception.TenantHasActiveContractsException;
import com.rentas.properties.api.exception.TenantNotFoundException;
import com.rentas.properties.api.exception.UnauthorizedAccessException;
import com.rentas.properties.business.services.TenantService;
import com.rentas.properties.dao.entity.Organization;
import com.rentas.properties.dao.entity.Tenant;
import com.rentas.properties.dao.entity.User;
import com.rentas.properties.dao.repository.TenantRepository;
import com.rentas.properties.dao.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TenantServiceImpl implements TenantService {

    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public TenantDetailResponse createTenant(CreateTenantRequest request) {
        log.info("Creando arrendatario: {} - Teléfono: {}", request.getFullName(), request.getPhone());

        User currentUser = getCurrentUser();
        validateUserHasOrganization(currentUser);

        Organization organization = currentUser.getOrganization();

        if (tenantRepository.existsByPhoneAndOrganization_Id(request.getPhone(), organization.getId())) {
            log.warn("Ya existe un arrendatario con el teléfono '{}' en la organización {}",
                    request.getPhone(), organization.getId());
            throw new TenantAlreadyExistsException(
                    "Ya existe un arrendatario con el teléfono '" + request.getPhone() + "' en tu organización"
            );
        }

        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            if (tenantRepository.existsByEmailAndOrganization_Id(request.getEmail(), organization.getId())) {
                log.warn("Ya existe un arrendatario con el email '{}' en la organización {}",
                        request.getEmail(), organization.getId());
                throw new TenantAlreadyExistsException(
                        "Ya existe un arrendatario con el email '" + request.getEmail() + "' en tu organización"
                );
            }
        }

        Tenant tenant = Tenant.builder()
                .organization(organization)
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .email(request.getEmail())
                .ineNumber(request.getIneNumber())
                .ineImageUrl(request.getIneImageUrl())
                .inePublicId(request.getInePublicId())
                .numberOfOccupants(request.getNumberOfOccupants() != null ? request.getNumberOfOccupants() : 1)
                .notes(request.getNotes())
                .isActive(true)
                .build();

        Tenant savedTenant = tenantRepository.save(tenant);
        log.info("Arrendatario creado exitosamente con ID: {}", savedTenant.getId());

        return mapToDetailResponse(savedTenant);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TenantResponse> getAllTenants(boolean includeInactive) {
        log.info("Obteniendo todos los arrendatarios - includeInactive: {}", includeInactive);

        User currentUser = getCurrentUser();
        validateUserHasOrganization(currentUser);

        UUID organizationId = currentUser.getOrganization().getId();

        List<Tenant> tenants;
        if (includeInactive) {
            tenants = tenantRepository.findByOrganization_Id(organizationId);
        } else {
            tenants = tenantRepository.findByOrganization_IdAndIsActiveTrue(organizationId);
        }

        log.debug("Se encontraron {} arrendatarios", tenants.size());

        return tenants.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public TenantDetailResponse getTenantById(UUID id) {
        log.info("Obteniendo arrendatario con ID: {}", id);

        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new TenantNotFoundException("Arrendatario no encontrado con ID: " + id));

        User currentUser = getCurrentUser();
        validateUserCanAccessTenant(currentUser, tenant);

        return mapToDetailResponse(tenant);
    }

    @Override
    @Transactional
    public TenantDetailResponse updateTenant(UUID id, UpdateTenantRequest request) {
        log.info("Actualizando arrendatario con ID: {}", id);

        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new TenantNotFoundException("Arrendatario no encontrado con ID: " + id));

        User currentUser = getCurrentUser();
        validateUserCanAccessTenant(currentUser, tenant);

        if (request.getFullName() != null) {
            tenant.setFullName(request.getFullName());
        }

        if (request.getPhone() != null && !request.getPhone().equals(tenant.getPhone())) {
            if (tenantRepository.existsByPhoneAndOrganization_Id(
                    request.getPhone(), tenant.getOrganization().getId())) {
                throw new TenantAlreadyExistsException(
                        "Ya existe un arrendatario con el teléfono '" + request.getPhone() + "' en tu organización"
                );
            }
            tenant.setPhone(request.getPhone());
        }

        if (request.getEmail() != null) {
            if (!request.getEmail().isEmpty() && !request.getEmail().equals(tenant.getEmail())) {
                if (tenantRepository.existsByEmailAndOrganization_Id(
                        request.getEmail(), tenant.getOrganization().getId())) {
                    throw new TenantAlreadyExistsException(
                            "Ya existe un arrendatario con el email '" + request.getEmail() + "' en tu organización"
                    );
                }
            }
            tenant.setEmail(request.getEmail());
        }

        if (request.getIneNumber() != null) {
            tenant.setIneNumber(request.getIneNumber());
        }

        if (request.getIneImageUrl() != null) {
            tenant.setIneImageUrl(request.getIneImageUrl());
        }

        if (request.getInePublicId() != null) {
            tenant.setInePublicId(request.getInePublicId());
        }

        if (request.getNumberOfOccupants() != null) {
            tenant.setNumberOfOccupants(request.getNumberOfOccupants());
        }

        if (request.getNotes() != null) {
            tenant.setNotes(request.getNotes());
        }

        Tenant updatedTenant = tenantRepository.save(tenant);
        log.info("Arrendatario actualizado exitosamente: {}", updatedTenant.getFullName());

        return mapToDetailResponse(updatedTenant);
    }

    @Override
    @Transactional
    public void deleteTenant(UUID id) {
        log.info("Eliminando arrendatario con ID: {}", id);

        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new TenantNotFoundException("Arrendatario no encontrado con ID: " + id));

        User currentUser = getCurrentUser();
        validateUserCanAccessTenant(currentUser, tenant);

        int activeContractsCount = tenant.getActiveContractsCount();
        if (activeContractsCount > 0) {
            log.warn("No se puede eliminar el arrendatario {} porque tiene {} contratos activos",
                    id, activeContractsCount);
            throw new TenantHasActiveContractsException(
                    "No se puede eliminar el arrendatario porque tiene " + activeContractsCount + " contratos activos"
            );
        }

        tenant.setIsActive(false);
        tenantRepository.save(tenant);

        log.info("Arrendatario desactivado exitosamente: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TenantResponse> searchTenantsByName(String name) {
        log.info("Buscando arrendatarios por nombre: {}", name);

        User currentUser = getCurrentUser();
        validateUserHasOrganization(currentUser);

        UUID organizationId = currentUser.getOrganization().getId();

        List<Tenant> tenants = tenantRepository.findByFullNameContainingIgnoreCaseAndOrganization_Id(
                name, organizationId
        );

        log.debug("Se encontraron {} arrendatarios con el nombre '{}'", tenants.size(), name);

        return tenants.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public TenantDetailResponse getTenantByPhone(String phone) {
        log.info("Buscando arrendatario por teléfono: {}", phone);

        User currentUser = getCurrentUser();
        validateUserHasOrganization(currentUser);

        UUID organizationId = currentUser.getOrganization().getId();

        Tenant tenant = tenantRepository.findByPhoneAndOrganization_Id(phone, organizationId)
                .orElseThrow(() -> new TenantNotFoundException(
                        "No se encontró un arrendatario con el teléfono '" + phone + "' en tu organización"
                ));

        return mapToDetailResponse(tenant);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TenantResponse> getActiveTenants() {
        log.info("Obteniendo arrendatarios activos");

        User currentUser = getCurrentUser();
        validateUserHasOrganization(currentUser);

        UUID organizationId = currentUser.getOrganization().getId();

        List<Tenant> tenants = tenantRepository.findByOrganization_IdAndIsActiveTrue(organizationId);

        log.debug("Se encontraron {} arrendatarios activos", tenants.size());

        return tenants.stream()
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

    private void validateUserHasOrganization(User user) {
        if (user.getOrganization() == null) {
            log.warn("Usuario {} no tiene organización asignada", user.getEmail());
            throw new UnauthorizedAccessException("Debes pertenecer a una organización para realizar esta acción");
        }
    }

    private void validateUserCanAccessTenant(User user, Tenant tenant) {
        if (user.getOrganization() == null) {
            throw new UnauthorizedAccessException("Debes pertenecer a una organización");
        }

        if (!user.getOrganization().getId().equals(tenant.getOrganization().getId())) {
            log.warn("Usuario {} intentó acceder a arrendatario {} de otra organización",
                    user.getEmail(), tenant.getId());
            throw new UnauthorizedAccessException("No tienes acceso a este arrendatario");
        }
    }

    private TenantResponse mapToResponse(Tenant tenant) {
        return TenantResponse.builder()
                .id(tenant.getId())
                .fullName(tenant.getFullName())
                .phone(tenant.getPhone())
                .email(tenant.getEmail())
                .numberOfOccupants(tenant.getNumberOfOccupants())
                .hasINE(tenant.hasINE())
                .isActive(tenant.getIsActive())
                .activeContractsCount(tenant.getActiveContractsCount())
                .createdAt(tenant.getCreatedAt())
                .build();
    }

    private TenantDetailResponse mapToDetailResponse(Tenant tenant) {
        return TenantDetailResponse.builder()
                .id(tenant.getId())
                .organizationId(tenant.getOrganization().getId())
                .organizationName(tenant.getOrganization().getName())
                .fullName(tenant.getFullName())
                .phone(tenant.getPhone())
                .email(tenant.getEmail())
                .ineNumber(tenant.getIneNumber())
                .ineImageUrl(tenant.getIneImageUrl())
                .inePublicId(tenant.getInePublicId())
                .numberOfOccupants(tenant.getNumberOfOccupants())
                .notes(tenant.getNotes())
                .isActive(tenant.getIsActive())
                .activeContractsCount(tenant.getActiveContractsCount())
                .createdAt(tenant.getCreatedAt())
                .updatedAt(tenant.getUpdatedAt())
                .createdBy(tenant.getCreatedBy())
                .build();
    }
}