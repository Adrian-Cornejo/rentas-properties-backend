package com.rentas.properties.business.services;

import com.rentas.properties.api.dto.response.PublicPropertyResponse;

import java.util.UUID;

public interface PublicPropertyService {

    PublicPropertyResponse getPublicPropertyById(UUID propertyId);
}