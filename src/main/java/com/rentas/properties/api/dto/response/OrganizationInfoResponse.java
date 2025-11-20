package com.rentas.properties.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Basic organization information for the current user")
public class OrganizationInfoResponse {

    @Schema(description = "Organization ID", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;

    @Schema(description = "Organization name", example = "Inmobiliaria XYZ")
    private String name;

    @Schema(description = "Organization logo URL")
    private String logoUrl;

    @Schema(description = "Subscription plan", example = "BASICO", allowableValues = {"BASICO", "INTERMEDIO", "SUPERIOR"})
    private String subscriptionPlan;

    @Schema(description = "Subscription status", example = "ACTIVE", allowableValues = {"TRIAL", "ACTIVE", "EXPIRED", "CANCELLED"})
    private String subscriptionStatus;

    @Schema(description = "Maximum properties allowed", example = "5")
    private Integer maxProperties;

    @Schema(description = "Current properties count", example = "3")
    private Integer currentPropertiesCount;

    @Schema(description = "Maximum users allowed", example = "3")
    private Integer maxUsers;

    @Schema(description = "Current users count", example = "2")
    private Integer currentUsersCount;

    @Schema(description = "Primary color for branding", example = "#3B82F6")
    private String primaryColor;

    @Schema(description = "Secondary color for branding", example = "#10B981")
    private String secondaryColor;
}