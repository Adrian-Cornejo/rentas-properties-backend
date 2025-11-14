package com.rentas.properties.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private UUID id;
    private String email;
    private String fullName;
    private String phone;
    private String role;
    private Boolean isActive;
    private String accountStatus;
    private UUID organizationId;
    private String organizationName;
    private LocalDateTime lastLogin;
    private LocalDateTime createdAt;
}