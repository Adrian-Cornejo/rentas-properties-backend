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
public class OrganizationStatsResponse {

    private UUID organizationId;
    private String organizationName;

    private Integer currentUsersCount;
    private Integer maxUsers;
    private Integer usersAvailable;
    private Double usersPercentage;

    private Integer currentPropertiesCount;
    private Integer maxProperties;
    private Integer propertiesAvailable;
    private Double propertiesPercentage;

    private String subscriptionStatus;
    private String subscriptionPlan;
    private LocalDateTime trialEndsAt;
    private LocalDateTime subscriptionEndsAt;

    private Boolean nearUserLimit;
    private Boolean nearPropertyLimit;
    private Long daysUntilTrialEnds;
    private Long daysUntilSubscriptionEnds;

    private Boolean isActive;
}