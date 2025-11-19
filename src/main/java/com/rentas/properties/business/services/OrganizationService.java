package com.rentas.properties.business.services;

import com.rentas.properties.api.dto.request.CreateOrganizationRequest;
import com.rentas.properties.api.dto.request.UpdateOrganizationRequest;
import com.rentas.properties.api.dto.response.OrganizationDetailResponse;
import com.rentas.properties.api.dto.response.OrganizationResponse;
import com.rentas.properties.api.dto.response.OrganizationStatsResponse;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrganizationService {

    OrganizationDetailResponse createOrganization(CreateOrganizationRequest request);

    List<OrganizationResponse> getAllOrganizations();

    OrganizationDetailResponse getOrganizationById(UUID id);

    OrganizationDetailResponse updateOrganization(UUID id, UpdateOrganizationRequest request);

    void deleteOrganization(UUID id);

    OrganizationDetailResponse regenerateInvitationCode(UUID id);

    OrganizationResponse validateInvitationCode(String code);

    OrganizationStatsResponse getOrganizationStats(UUID id);

    List<OrganizationResponse> getActiveOrganizations();

    Optional<OrganizationDetailResponse> getMyOrganization();

    OrganizationStatsResponse getMyOrganizationStats();
}