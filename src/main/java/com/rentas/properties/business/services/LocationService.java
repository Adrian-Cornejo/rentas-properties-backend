package com.rentas.properties.business.services;

import com.rentas.properties.api.dto.request.CreateLocationRequest;
import com.rentas.properties.api.dto.request.UpdateLocationRequest;
import com.rentas.properties.api.dto.response.LocationResponse;

import java.util.List;
import java.util.UUID;

public interface LocationService {

    List<LocationResponse> findAll(boolean includeInactive);

    LocationResponse findById(UUID id);

    LocationResponse create(CreateLocationRequest request);

    LocationResponse update(UUID id, UpdateLocationRequest request);

    void delete(UUID id);

    List<LocationResponse> findByCity(String city);

    boolean existsByName(String name);

    void validateLocationCanBeDeleted(UUID id);
}