package com.rentas.properties.dao.repository;

import com.rentas.properties.dao.entity.MaintenanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface MaintenanceRecordRepository extends JpaRepository<MaintenanceRecord, UUID> {

    List<MaintenanceRecord> findByPropertyId(UUID propertyId);

    List<MaintenanceRecord> findByStatus(String status);

    List<MaintenanceRecord> findByMaintenanceType(String maintenanceType);

    @Query("SELECT m FROM MaintenanceRecord m WHERE m.status = 'PENDIENTE'")
    List<MaintenanceRecord> findPendingMaintenance();

    @Query("SELECT m FROM MaintenanceRecord m " +
            "WHERE m.maintenanceType = 'EMERGENCIA' AND m.status IN ('PENDIENTE', 'EN_PROCESO')")
    List<MaintenanceRecord> findEmergencyMaintenancePending();

    @Query("SELECT m FROM MaintenanceRecord m " +
            "WHERE m.maintenanceDate < :today AND m.status IN ('PENDIENTE', 'EN_PROCESO')")
    List<MaintenanceRecord> findOverdueMaintenance(@Param("today") LocalDate today);

    List<MaintenanceRecord> findByOrganization_Id(UUID organizationId);

    List<MaintenanceRecord> findByOrganization_IdAndStatus(UUID organizationId, String status);

    @Query("SELECT m FROM MaintenanceRecord m WHERE m.organization.id = :organizationId AND m.status = 'PENDIENTE'")
    List<MaintenanceRecord> findPendingMaintenanceByOrganization(@Param("organizationId") UUID organizationId);

    @Query("SELECT m FROM MaintenanceRecord m " +
            "WHERE m.organization.id = :organizationId " +
            "AND m.maintenanceType = 'EMERGENCIA' AND m.status IN ('PENDIENTE', 'EN_PROCESO')")
    List<MaintenanceRecord> findEmergencyMaintenancePendingByOrganization(@Param("organizationId") UUID organizationId);

    @Query("SELECT m FROM MaintenanceRecord m " +
            "WHERE m.organization.id = :organizationId " +
            "AND m.maintenanceDate < :today AND m.status IN ('PENDIENTE', 'EN_PROCESO')")
    List<MaintenanceRecord> findOverdueMaintenanceByOrganization(
            @Param("organizationId") UUID organizationId,
            @Param("today") LocalDate today
    );

    @Query("SELECT COUNT(m) FROM MaintenanceRecord m " +
            "WHERE m.organization.id = :organizationId AND m.status = :status")
    Long countByOrganizationIdAndStatus(
            @Param("organizationId") UUID organizationId,
            @Param("status") String status
    );
}