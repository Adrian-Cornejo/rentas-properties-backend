package com.rentas.properties.business.services;

import com.rentas.properties.api.dto.request.CreateTenantRequest;
import com.rentas.properties.api.dto.request.UpdateTenantRequest;
import com.rentas.properties.api.dto.response.TenantDetailResponse;
import com.rentas.properties.api.dto.response.TenantResponse;

import java.util.List;
import java.util.UUID;

public interface TenantService {

    TenantDetailResponse createTenant(CreateTenantRequest request);

    List<TenantResponse> getAllTenants(boolean includeInactive);

    TenantDetailResponse getTenantById(UUID id);

    TenantDetailResponse updateTenant(UUID id, UpdateTenantRequest request);

    void deleteTenant(UUID id);

    List<TenantResponse> searchTenantsByName(String name);

    TenantDetailResponse getTenantByPhone(String phone);

    List<TenantResponse> getActiveTenants();

    void deleteTenantIneImage(UUID tenantId);
}