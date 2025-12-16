// src/main/java/com/rentas/properties/dao/entity/SubscriptionFeature.java
package com.rentas.properties.dao.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "subscription_features")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionFeature {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private SubscriptionPlan subscriptionPlan;

    @Column(name = "feature_category", length = 100)
    private String featureCategory; // CORE, NOTIFICATIONS, REPORTS, INTEGRATIONS, SUPPORT

    @Column(name = "feature_name", nullable = false)
    private String featureName;

    @Column(name = "feature_description", columnDefinition = "TEXT")
    private String featureDescription;

    @Column(name = "is_included")
    private Boolean isIncluded = true;

    @Column(name = "is_highlight")
    private Boolean isHighlight = false;

    @Column(name = "display_order")
    private Integer displayOrder = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}