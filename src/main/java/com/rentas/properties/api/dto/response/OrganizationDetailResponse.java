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
public class OrganizationDetailResponse {

    private UUID id;
    private String name;
    private String description;
    private String logoUrl;
    private String logoPublicId;
    private String primaryColor;
    private String secondaryColor;
    private String invitationCode;
    private Boolean codeIsReusable;
    private OwnerDto owner;
    private Integer maxUsers;
    private Integer maxProperties;
    private Integer currentUsersCount;
    private Integer currentPropertiesCount;
    private String subscriptionStatus;
    private String subscriptionPlan;
    private UUID subscriptionId;
    private LocalDateTime trialEndsAt;
    private LocalDateTime subscriptionStartedAt;
    private LocalDateTime subscriptionEndsAt;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long daysUntilTrialEnds;
    private Long daysUntilSubscriptionEnds;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OwnerDto {
        private UUID id;
        private String email;
        private String fullName;
        private String phone;
    }
}