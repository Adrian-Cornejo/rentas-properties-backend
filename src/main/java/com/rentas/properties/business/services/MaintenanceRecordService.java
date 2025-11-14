package com.rentas.properties.business.services;

import com.rentas.properties.api.dto.request.CreateMaintenanceRecordRequest;
import com.rentas.properties.api.dto.request.UpdateMaintenanceRecordRequest;
import com.rentas.properties.api.dto.response.MaintenanceRecordDetailResponse;
import com.rentas.properties.api.dto.response.MaintenanceRecordResponse;
import com.rentas.properties.api.dto.response.MaintenanceRecordSummaryResponse;

import java.util.List;
import java.util.UUID;

public interface MaintenanceRecordService {

    MaintenanceRecordDetailResponse createMaintenanceRecord(CreateMaintenanceRecordRequest request);

    List<MaintenanceRecordResponse> getAllMaintenanceRecords();

    MaintenanceRecordDetailResponse getMaintenanceRecordById(UUID id);

    MaintenanceRecordDetailResponse updateMaintenanceRecord(UUID id, UpdateMaintenanceRecordRequest request);

    void deleteMaintenanceRecord(UUID id);

    MaintenanceRecordDetailResponse addImage(UUID id, String imageUrl, String imagePublicId, 
                                             String imageType, String description);

    void deleteImage(UUID imageId);

    List<MaintenanceRecordResponse> getMaintenanceRecordsByProperty(UUID propertyId);

    List<MaintenanceRecordResponse> getMaintenanceRecordsByContract(UUID contractId);

    List<MaintenanceRecordResponse> getMaintenanceRecordsByStatus(String status);

    List<MaintenanceRecordResponse> getMaintenanceRecordsByType(String type);

    List<MaintenanceRecordResponse> getMaintenanceRecordsByCategory(String category);

    List<MaintenanceRecordResponse> getPendingMaintenanceRecords();

    MaintenanceRecordDetailResponse markAsCompleted(UUID id, String actualCost);

    MaintenanceRecordSummaryResponse getMaintenanceRecordsSummary();
}
