package com.rentas.properties.business.services.impl;

import com.rentas.properties.api.dto.request.CreateMaintenanceRecordRequest;
import com.rentas.properties.api.dto.request.UpdateMaintenanceRecordRequest;
import com.rentas.properties.api.dto.response.MaintenanceRecordDetailResponse;
import com.rentas.properties.api.dto.response.MaintenanceRecordResponse;
import com.rentas.properties.api.dto.response.MaintenanceRecordSummaryResponse;
import com.rentas.properties.api.exception.*;
import com.rentas.properties.business.services.MaintenanceRecordService;
import com.rentas.properties.dao.entity.*;
import com.rentas.properties.dao.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MaintenanceRecordServiceImpl implements MaintenanceRecordService {

    private final MaintenanceRecordRepository maintenanceRecordRepository;
    private final PropertyRepository propertyRepository;
    private final ContractRepository contractRepository;
    private final MaintenanceImageRepository maintenanceImageRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public MaintenanceRecordDetailResponse createMaintenanceRecord(CreateMaintenanceRecordRequest request) {
        log.info("Creando registro de mantenimiento para propiedad ID: {}", request.getPropertyId());

        User currentUser = getCurrentUser();
        validateUserHasOrganization(currentUser);

        Organization organization = currentUser.getOrganization();

        Property property = propertyRepository.findById(request.getPropertyId())
                .orElseThrow(() -> new PropertyNotFoundException(
                        "Propiedad no encontrada con ID: " + request.getPropertyId()));

        validateUserCanAccessProperty(currentUser, property);

        Contract contract = null;
        if (request.getContractId() != null) {
            contract = contractRepository.findById(request.getContractId())
                    .orElseThrow(() -> new ContractNotFoundException(
                            "Contrato no encontrado con ID: " + request.getContractId()));

            validateUserCanAccessContract(currentUser, contract);

            if (!contract.getProperty().getId().equals(property.getId())) {
                throw new IllegalArgumentException(
                        "El contrato no pertenece a la propiedad especificada");
            }
        }

        MaintenanceRecord maintenanceRecord = MaintenanceRecord.builder()
                .organization(organization)
                .property(property)
                .contract(contract)
                .title(request.getTitle())
                .description(request.getDescription())
                .maintenanceType(request.getMaintenanceType())
                .category(request.getCategory())
                .maintenanceDate(request.getMaintenanceDate())
                .estimatedCost(request.getEstimatedCost())
                .status("PENDIENTE")
                .assignedTo(request.getAssignedTo())
                .notes(request.getNotes())
                .build();

        MaintenanceRecord savedRecord = maintenanceRecordRepository.save(maintenanceRecord);
        log.info("Registro de mantenimiento creado con ID: {}", savedRecord.getId());

        return mapToDetailResponse(savedRecord);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MaintenanceRecordResponse> getAllMaintenanceRecords() {
        log.info("Obteniendo todos los registros de mantenimiento");

        User currentUser = getCurrentUser();
        validateUserHasOrganization(currentUser);

        UUID organizationId = currentUser.getOrganization().getId();

        List<MaintenanceRecord> records = maintenanceRecordRepository.findByOrganization_Id(organizationId);

        log.debug("Se encontraron {} registros", records.size());

        return records.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public MaintenanceRecordDetailResponse getMaintenanceRecordById(UUID id) {
        log.info("Obteniendo registro de mantenimiento con ID: {}", id);

        MaintenanceRecord record = maintenanceRecordRepository.findById(id)
                .orElseThrow(() -> new MaintenanceRecordNotFoundException(
                        "Registro de mantenimiento no encontrado con ID: " + id));

        User currentUser = getCurrentUser();
        validateUserCanAccessMaintenanceRecord(currentUser, record);

        return mapToDetailResponse(record);
    }

    @Override
    @Transactional
    public MaintenanceRecordDetailResponse updateMaintenanceRecord(UUID id, UpdateMaintenanceRecordRequest request) {
        log.info("Actualizando registro de mantenimiento con ID: {}", id);

        MaintenanceRecord record = maintenanceRecordRepository.findById(id)
                .orElseThrow(() -> new MaintenanceRecordNotFoundException(
                        "Registro de mantenimiento no encontrado con ID: " + id));

        User currentUser = getCurrentUser();
        validateUserCanAccessMaintenanceRecord(currentUser, record);

        if (request.getTitle() != null) {
            record.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            record.setDescription(request.getDescription());
        }
        if (request.getMaintenanceType() != null) {
            record.setMaintenanceType(request.getMaintenanceType());
        }
        if (request.getCategory() != null) {
            record.setCategory(request.getCategory());
        }
        if (request.getMaintenanceDate() != null) {
            record.setMaintenanceDate(request.getMaintenanceDate());
        }
        if (request.getEstimatedCost() != null) {
            record.setEstimatedCost(request.getEstimatedCost());
        }
        if (request.getActualCost() != null) {
            record.setActualCost(request.getActualCost());
        }
        if (request.getStatus() != null) {
            record.setStatus(request.getStatus());

            if ("COMPLETADO".equals(request.getStatus()) && record.getCompletedDate() == null) {
                record.setCompletedDate(LocalDate.now());
            }
        }
        if (request.getAssignedTo() != null) {
            record.setAssignedTo(request.getAssignedTo());
        }
        if (request.getNotes() != null) {
            record.setNotes(request.getNotes());
        }

        MaintenanceRecord updatedRecord = maintenanceRecordRepository.save(record);
        log.info("Registro de mantenimiento actualizado exitosamente");

        return mapToDetailResponse(updatedRecord);
    }

    @Override
    @Transactional
    public void deleteMaintenanceRecord(UUID id) {
        log.info("Eliminando registro de mantenimiento con ID: {}", id);

        MaintenanceRecord record = maintenanceRecordRepository.findById(id)
                .orElseThrow(() -> new MaintenanceRecordNotFoundException(
                        "Registro de mantenimiento no encontrado con ID: " + id));

        User currentUser = getCurrentUser();
        validateUserCanAccessMaintenanceRecord(currentUser, record);

        maintenanceImageRepository.deleteByMaintenanceRecordId(id);

        maintenanceRecordRepository.delete(record);
        log.info("Registro de mantenimiento eliminado exitosamente");
    }

    @Override
    @Transactional
    public MaintenanceRecordDetailResponse addImage(UUID id, String imageUrl, String imagePublicId,
                                                    String imageType, String description) {
        log.info("Agregando imagen tipo {} al registro {}", imageType, id);

        MaintenanceRecord record = maintenanceRecordRepository.findById(id)
                .orElseThrow(() -> new MaintenanceRecordNotFoundException(
                        "Registro de mantenimiento no encontrado con ID: " + id));

        User currentUser = getCurrentUser();
        validateUserCanAccessMaintenanceRecord(currentUser, record);

        Organization organization = record.getOrganization();

        if (!organization.hasFeature("MAINTENANCE_PHOTOS")) {
            log.warn("Plan {} no permite fotos de mantenimiento", organization.getPlanCode());
            throw new FeatureNotAvailableException(
                    "Tu plan " + organization.getPlanCode() + " no permite agregar fotos a los registros de mantenimiento. " +
                            "Por favor, mejora tu plan para habilitar esta funcionalidad."
            );
        }

        if (!imageType.matches("^(ANTES|DESPUES|EVIDENCIA)$")) {
            throw new IllegalArgumentException(
                    "El tipo de imagen debe ser: ANTES, DESPUES o EVIDENCIA");
        }

        MaintenanceImage image = MaintenanceImage.builder()
                .maintenanceRecord(record)
                .imageUrl(imageUrl)
                .imagePublicId(imagePublicId)
                .imageType(imageType)
                .description(description)
                .build();

        maintenanceImageRepository.save(image);
        log.info("Imagen agregada exitosamente");

        return mapToDetailResponse(record);
    }

    @Override
    @Transactional
    public void deleteImage(UUID imageId) {
        log.info("Eliminando imagen con ID: {}", imageId);

        MaintenanceImage image = maintenanceImageRepository.findById(imageId)
                .orElseThrow(() -> new MaintenanceImageNotFoundException(
                        "Imagen no encontrada con ID: " + imageId));

        User currentUser = getCurrentUser();
        validateUserCanAccessMaintenanceRecord(currentUser, image.getMaintenanceRecord());

        maintenanceImageRepository.delete(image);
        log.info("Imagen eliminada exitosamente");
    }

    @Override
    @Transactional(readOnly = true)
    public List<MaintenanceRecordResponse> getMaintenanceRecordsByProperty(UUID propertyId) {
        log.info("Obteniendo registros de mantenimiento de la propiedad: {}", propertyId);

        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new PropertyNotFoundException(
                        "Propiedad no encontrada con ID: " + propertyId));

        User currentUser = getCurrentUser();
        validateUserCanAccessProperty(currentUser, property);

        List<MaintenanceRecord> records = maintenanceRecordRepository.findByPropertyId(propertyId);

        return records.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MaintenanceRecordResponse> getMaintenanceRecordsByContract(UUID contractId) {
        log.info("Obteniendo registros de mantenimiento del contrato: {}", contractId);

        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new ContractNotFoundException(
                        "Contrato no encontrado con ID: " + contractId));

        User currentUser = getCurrentUser();
        validateUserCanAccessContract(currentUser, contract);

        List<MaintenanceRecord> records = maintenanceRecordRepository.findByContract_Id(contractId);

        return records.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MaintenanceRecordResponse> getMaintenanceRecordsByStatus(String status) {
        log.info("Obteniendo registros por estado: {}", status);

        User currentUser = getCurrentUser();
        validateUserHasOrganization(currentUser);

        UUID organizationId = currentUser.getOrganization().getId();

        List<MaintenanceRecord> records = maintenanceRecordRepository.findByStatusAndOrganization_Id(
                status, organizationId);

        return records.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MaintenanceRecordResponse> getMaintenanceRecordsByType(String type) {
        log.info("Obteniendo registros por tipo: {}", type);

        User currentUser = getCurrentUser();
        validateUserHasOrganization(currentUser);

        UUID organizationId = currentUser.getOrganization().getId();

        List<MaintenanceRecord> records = maintenanceRecordRepository.findByMaintenanceTypeAndOrganization_Id(
                type, organizationId);

        return records.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MaintenanceRecordResponse> getMaintenanceRecordsByCategory(String category) {
        log.info("Obteniendo registros por categoría: {}", category);

        User currentUser = getCurrentUser();
        validateUserHasOrganization(currentUser);

        UUID organizationId = currentUser.getOrganization().getId();

        List<MaintenanceRecord> records = maintenanceRecordRepository.findByCategoryAndOrganization_Id(
                category, organizationId);

        return records.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MaintenanceRecordResponse> getPendingMaintenanceRecords() {
        log.info("Obteniendo registros pendientes");

        User currentUser = getCurrentUser();
        validateUserHasOrganization(currentUser);

        UUID organizationId = currentUser.getOrganization().getId();

        List<MaintenanceRecord> records = maintenanceRecordRepository.findPendingByOrganization(organizationId);

        return records.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public MaintenanceRecordDetailResponse markAsCompleted(UUID id, String actualCost) {
        log.info("Marcando registro {} como completado", id);

        MaintenanceRecord record = maintenanceRecordRepository.findById(id)
                .orElseThrow(() -> new MaintenanceRecordNotFoundException(
                        "Registro de mantenimiento no encontrado con ID: " + id));

        User currentUser = getCurrentUser();
        validateUserCanAccessMaintenanceRecord(currentUser, record);

        record.setStatus("COMPLETADO");
        record.setCompletedDate(LocalDate.now());

        if (actualCost != null && !actualCost.isEmpty()) {
            try {
                record.setActualCost(new BigDecimal(actualCost));
            } catch (NumberFormatException e) {
                log.warn("Costo actual inválido: {}", actualCost);
            }
        }

        MaintenanceRecord updatedRecord = maintenanceRecordRepository.save(record);
        log.info("Registro marcado como COMPLETADO exitosamente");

        return mapToDetailResponse(updatedRecord);
    }

    @Override
    @Transactional(readOnly = true)
    public MaintenanceRecordSummaryResponse getMaintenanceRecordsSummary() {
        log.info("Obteniendo resumen de registros de mantenimiento");

        User currentUser = getCurrentUser();
        validateUserHasOrganization(currentUser);

        UUID organizationId = currentUser.getOrganization().getId();

        List<MaintenanceRecord> allRecords = maintenanceRecordRepository.findByOrganization_Id(organizationId);

        long totalRecords = allRecords.size();

        long pendingRecords = allRecords.stream()
                .filter(r -> "PENDIENTE".equals(r.getStatus()) || "EN_PROCESO".equals(r.getStatus()))
                .count();

        long completedRecords = allRecords.stream()
                .filter(r -> "COMPLETADO".equals(r.getStatus()))
                .count();

        BigDecimal totalEstimatedCost = allRecords.stream()
                .map(r -> r.getEstimatedCost() != null ? r.getEstimatedCost() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalActualCost = allRecords.stream()
                .filter(r -> r.getActualCost() != null)
                .map(MaintenanceRecord::getActualCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long preventiveCount = allRecords.stream()
                .filter(r -> "PREVENTIVO".equals(r.getMaintenanceType()))
                .count();

        long correctiveCount = allRecords.stream()
                .filter(r -> "CORRECTIVO".equals(r.getMaintenanceType()))
                .count();

        long emergencyCount = allRecords.stream()
                .filter(r -> "EMERGENCIA".equals(r.getMaintenanceType()))
                .count();

        return MaintenanceRecordSummaryResponse.builder()
                .totalRecords(totalRecords)
                .pendingRecords(pendingRecords)
                .completedRecords(completedRecords)
                .totalEstimatedCost(totalEstimatedCost)
                .totalActualCost(totalActualCost)
                .preventiveCount(preventiveCount)
                .correctiveCount(correctiveCount)
                .emergencyCount(emergencyCount)
                .build();
    }


    private User getCurrentUser() {
        String email = ((UserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal()).getUsername();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + email));
    }

    private void validateUserHasOrganization(User user) {
        if (user.getOrganization() == null) {
            throw new UnauthorizedAccessException(
                    "Debes pertenecer a una organización para realizar esta acción");
        }
    }

    private void validateUserCanAccessMaintenanceRecord(User user, MaintenanceRecord record) {
        if (user.getOrganization() == null) {
            throw new UnauthorizedAccessException("No perteneces a ninguna organización");
        }

        if (!record.getOrganization().getId().equals(user.getOrganization().getId())) {
            log.warn("Usuario {} intentó acceder al registro {} de otra organización",
                    user.getId(), record.getId());
            throw new UnauthorizedAccessException(
                    "No tienes permisos para acceder a este registro");
        }
    }

    private void validateUserCanAccessProperty(User user, Property property) {
        if (user.getOrganization() == null) {
            throw new UnauthorizedAccessException("No perteneces a ninguna organización");
        }

        if (!property.getOrganization().getId().equals(user.getOrganization().getId())) {
            throw new UnauthorizedAccessException(
                    "No tienes permisos para acceder a esta propiedad");
        }
    }

    private void validateUserCanAccessContract(User user, Contract contract) {
        if (user.getOrganization() == null) {
            throw new UnauthorizedAccessException("No perteneces a ninguna organización");
        }

        if (!contract.getOrganization().getId().equals(user.getOrganization().getId())) {
            throw new UnauthorizedAccessException(
                    "No tienes permisos para acceder a este contrato");
        }
    }

    private MaintenanceRecordResponse mapToResponse(MaintenanceRecord record) {
        String propertyCode = record.getProperty().getPropertyCode();
        String propertyAddress = record.getProperty().getAddress();
        String contractNumber = record.getContract() != null ?
                record.getContract().getContractNumber() : null;

        int imageCount = maintenanceImageRepository.findByMaintenanceRecordId(record.getId()).size();

        return MaintenanceRecordResponse.builder()
                .id(record.getId())
                .propertyId(record.getProperty().getId())
                .propertyCode(propertyCode)
                .propertyAddress(propertyAddress)
                .contractId(record.getContract() != null ? record.getContract().getId() : null)
                .contractNumber(contractNumber)
                .title(record.getTitle())
                .maintenanceType(record.getMaintenanceType())
                .category(record.getCategory())
                .maintenanceDate(record.getMaintenanceDate())
                .completedDate(record.getCompletedDate())
                .estimatedCost(record.getEstimatedCost())
                .actualCost(record.getActualCost())
                .status(record.getStatus())
                .assignedTo(record.getAssignedTo())
                .imageCount(imageCount)
                .createdAt(record.getCreatedAt())
                .build();
    }

    private MaintenanceRecordDetailResponse mapToDetailResponse(MaintenanceRecord record) {
        MaintenanceRecordDetailResponse.PropertyDto propertyDto =
                MaintenanceRecordDetailResponse.PropertyDto.builder()
                        .id(record.getProperty().getId())
                        .propertyCode(record.getProperty().getPropertyCode())
                        .address(record.getProperty().getAddress())
                        .build();

        MaintenanceRecordDetailResponse.ContractDto contractDto = null;
        if (record.getContract() != null) {
            contractDto = MaintenanceRecordDetailResponse.ContractDto.builder()
                    .id(record.getContract().getId())
                    .contractNumber(record.getContract().getContractNumber())
                    .build();
        }

        List<MaintenanceImage> images = maintenanceImageRepository.findByMaintenanceRecordId(record.getId());
        List<MaintenanceRecordDetailResponse.ImageDto> imageDtos = images.stream()
                .map(img -> MaintenanceRecordDetailResponse.ImageDto.builder()
                        .id(img.getId())
                        .imageUrl(img.getImageUrl())
                        .imagePublicId(img.getImagePublicId())
                        .imageType(img.getImageType())
                        .description(img.getDescription())
                        .createdAt(img.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        return MaintenanceRecordDetailResponse.builder()
                .id(record.getId())
                .organizationId(record.getOrganization().getId())
                .property(propertyDto)
                .contract(contractDto)
                .title(record.getTitle())
                .description(record.getDescription())
                .maintenanceType(record.getMaintenanceType())
                .category(record.getCategory())
                .maintenanceDate(record.getMaintenanceDate())
                .completedDate(record.getCompletedDate())
                .estimatedCost(record.getEstimatedCost())
                .actualCost(record.getActualCost())
                .status(record.getStatus())
                .assignedTo(record.getAssignedTo())
                .notes(record.getNotes())
                .images(imageDtos)
                .createdAt(record.getCreatedAt())
                .updatedAt(record.getUpdatedAt())
                .createdBy(record.getCreatedBy())
                .build();
    }
}