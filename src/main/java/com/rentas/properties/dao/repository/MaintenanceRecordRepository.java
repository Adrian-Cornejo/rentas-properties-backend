package com.rentas.properties.dao.repository;

import com.rentas.properties.dao.entity.MaintenanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MaintenanceRecordRepository extends JpaRepository<MaintenanceRecord, UUID> {

    List<MaintenanceRecord> findByOrganization_Id(UUID organizationId);

    List<MaintenanceRecord> findByPropertyId(UUID propertyId);

    List<MaintenanceRecord> findByContract_Id(UUID contractId);

    List<MaintenanceRecord> findByStatus(String status);

    List<MaintenanceRecord> findByMaintenanceType(String maintenanceType);

    List<MaintenanceRecord> findByCategory(String category);

    List<MaintenanceRecord> findByStatusAndOrganization_Id(String status, UUID organizationId);

    List<MaintenanceRecord> findByMaintenanceTypeAndOrganization_Id(String maintenanceType, UUID organizationId);

    List<MaintenanceRecord> findByCategoryAndOrganization_Id(String category, UUID organizationId);

    @Query("SELECT mr FROM MaintenanceRecord mr WHERE mr.organization.id = :organizationId " +
            "AND (mr.status = 'PENDIENTE' OR mr.status = 'EN_PROCESO')")
    List<MaintenanceRecord> findPendingByOrganization(@Param("organizationId") UUID organizationId);

    @Query("SELECT COUNT(mr) FROM MaintenanceRecord mr WHERE mr.organization.id = :organizationId")
    Long countByOrganization_Id(@Param("organizationId") UUID organizationId);

    @Query("SELECT COUNT(mr) FROM MaintenanceRecord mr WHERE mr.organization.id = :organizationId " +
            "AND (mr.status = 'PENDIENTE' OR mr.status = 'EN_PROCESO')")
    Long countPendingByOrganization(@Param("organizationId") UUID organizationId);
}