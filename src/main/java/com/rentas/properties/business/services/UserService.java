package com.rentas.properties.business.services;

import com.rentas.properties.api.dto.request.JoinOrganizationRequest;
import com.rentas.properties.api.dto.request.UpdateUserRequest;
import com.rentas.properties.api.dto.response.UserDetailResponse;
import com.rentas.properties.api.dto.response.UserResponse;

import java.util.List;
import java.util.UUID;

public interface UserService {

    List<UserResponse> getAllUsers(boolean includeInactive);

    UserDetailResponse getUserById(UUID id);

    UserDetailResponse getCurrentUserProfile();

    UserDetailResponse updateUser(UUID id, UpdateUserRequest request);

    void deactivateUser(UUID id);

    void activateUser(UUID id);

    UserDetailResponse joinOrganization(JoinOrganizationRequest request);

    List<UserResponse> getUsersByOrganization();

    List<UserResponse> getUsersWithoutOrganization();

    List<UserResponse> getUsersByAccountStatus(String status);
}