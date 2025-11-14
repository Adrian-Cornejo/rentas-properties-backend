package com.rentas.properties.business.services;

import com.rentas.properties.api.dto.request.CreatePropertyRequest;
import com.rentas.properties.api.dto.request.UpdatePropertyRequest;
import com.rentas.properties.api.dto.response.PropertyDetailResponse;
import com.rentas.properties.api.dto.response.PropertyResponse;

import java.util.List;
import java.util.UUID;

public interface PropertyService {

    PropertyDetailResponse createProperty(CreatePropertyRequest request);

    List<PropertyResponse> getAllProperties(boolean includeInactive);

    PropertyDetailResponse getPropertyById(UUID id);

    PropertyDetailResponse getPropertyByCode(String code);

    PropertyDetailResponse updateProperty(UUID id, UpdatePropertyRequest request);

    void deleteProperty(UUID id);

    List<PropertyResponse> getPropertiesByStatus(String status);

    List<PropertyResponse> getPropertiesByLocation(UUID locationId);

    List<PropertyResponse> getPropertiesByType(String type);

    List<PropertyResponse> getAvailableProperties();

    List<PropertyResponse> getRentedProperties();
}