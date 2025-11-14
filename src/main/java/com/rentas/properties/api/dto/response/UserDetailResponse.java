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
public class UserDetailResponse {

    private UUID id;
    private String email;
    private String fullName;
    private String phone;
    private String role;
    private Boolean isActive;
    private String accountStatus;
    private OrganizationDto organization;
    private LocalDateTime organizationJoinedAt;
    private LocalDateTime lastLogin;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrganizationDto {
        private UUID id;
        private String name;
        private String invitationCode;
        private String subscriptionPlan;
        private String subscriptionStatus;
    }
}