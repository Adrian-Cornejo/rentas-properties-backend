package com.rentas.properties.business.services;

import com.rentas.properties.api.dto.request.CreateLocationRequest;
import com.rentas.properties.api.dto.request.UpdateLocationRequest;
import com.rentas.properties.api.dto.response.LocationDetailResponse;
import com.rentas.properties.api.dto.response.LocationResponse;

import java.util.List;
import java.util.UUID;

public interface LocationService {

    LocationDetailResponse createLocation(CreateLocationRequest request);

    List<LocationResponse> getAllLocations(boolean includeInactive);

    LocationDetailResponse getLocationById(UUID id);

    LocationDetailResponse updateLocation(UUID id, UpdateLocationRequest request);

    void deleteLocation(UUID id);

    List<LocationResponse> getLocationsByCity(String city);

    List<LocationResponse> getActiveLocations();
}