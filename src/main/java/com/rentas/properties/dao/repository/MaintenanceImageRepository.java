package com.rentas.properties.dao.repository;

import com.rentas.properties.dao.entity.MaintenanceImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MaintenanceImageRepository extends JpaRepository<MaintenanceImage, UUID> {

    List<MaintenanceImage> findByMaintenanceRecordId(UUID maintenanceId);

    @Query("SELECT mi FROM MaintenanceImage mi WHERE mi.maintenanceRecord.id = :maintenanceId AND mi.imageType = 'ANTES'")
    List<MaintenanceImage> findBeforeImagesByMaintenance(@Param("maintenanceId") UUID maintenanceId);

    @Query("SELECT mi FROM MaintenanceImage mi WHERE mi.maintenanceRecord.id = :maintenanceId AND mi.imageType = 'DESPUES'")
    List<MaintenanceImage> findAfterImagesByMaintenance(@Param("maintenanceId") UUID maintenanceId);

    void deleteByMaintenanceRecordId(UUID maintenanceId);
}