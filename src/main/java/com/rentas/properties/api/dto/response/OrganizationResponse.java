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
public class OrganizationResponse {

    private UUID id;
    private String name;
    private String description;
    private String logoUrl;
    private String primaryColor;
    private String secondaryColor;
    private String invitationCode;
    private Integer maxUsers;
    private Integer maxProperties;
    private Integer currentUsersCount;
    private Integer currentPropertiesCount;
    private String subscriptionStatus;
    private String subscriptionPlan;
    private Boolean isActive;
    private LocalDateTime createdAt;
}