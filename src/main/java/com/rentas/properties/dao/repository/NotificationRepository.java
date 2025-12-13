package com.rentas.properties.dao.repository;

import com.rentas.properties.dao.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    /**
     * Encuentra notificaciones pendientes que exceden 24 horas
     */
    @Query("SELECT n FROM Notification n WHERE n.status = 'PENDING' " +
            "AND n.createdAt < :threshold " +
            "AND n.retryCount < :maxRetries")
    List<Notification> findPendingNotificationsOlderThan(
            @Param("threshold") LocalDateTime threshold,
            @Param("maxRetries") int maxRetries
    );

    /**
     * Encuentra notificaciones por pago
     */
    List<Notification> findByRelatedPayment_Id(UUID paymentId);

    /**
     * Encuentra notificaciones por contrato
     */
    List<Notification> findByRelatedContract_Id(UUID contractId);

    /**
     * Cuenta notificaciones enviadas en un rango de fechas para una organización
     */
    @Query("SELECT COUNT(n) FROM Notification n " +
            "JOIN n.relatedPayment p " +
            "JOIN p.contract c " +
            "WHERE c.organization.id = :organizationId " +
            "AND n.sentAt BETWEEN :startDate AND :endDate " +
            "AND n.status IN ('SENT', 'DELIVERED')")
    Long countSentNotificationsByOrganizationAndDateRange(
            @Param("organizationId") UUID organizationId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    /**
     * Obtiene últimas notificaciones de una organización
     */
    @Query("SELECT n FROM Notification n " +
            "JOIN n.relatedPayment p " +
            "JOIN p.contract c " +
            "WHERE c.organization.id = :organizationId " +
            "ORDER BY n.createdAt DESC")
    List<Notification> findRecentByOrganization(@Param("organizationId") UUID organizationId);

    /**
     * Cuenta notificaciones por estado para una organización
     */
    @Query("SELECT n.status, COUNT(n) FROM Notification n " +
            "JOIN n.relatedPayment p " +
            "JOIN p.contract c " +
            "WHERE c.organization.id = :organizationId " +
            "GROUP BY n.status")
    List<Object[]> countByStatusAndOrganization(@Param("organizationId") UUID organizationId);

    /**
     * Obtiene datos para gráfica de notificaciones por día
     */
    @Query("SELECT CAST(n.sentAt AS date), n.status, COUNT(n) " +
            "FROM Notification n " +
            "JOIN n.relatedPayment p " +
            "JOIN p.contract c " +
            "WHERE c.organization.id = :organizationId " +
            "AND n.sentAt BETWEEN :startDate AND :endDate " +
            "GROUP BY CAST(n.sentAt AS date), n.status " +
            "ORDER BY CAST(n.sentAt AS date) DESC")
    List<Object[]> getChartDataByOrganization(
            @Param("organizationId") UUID organizationId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    /**
     * Cancela todas las notificaciones pendientes de un contrato
     */
    @Query("UPDATE Notification n SET n.status = 'CANCELLED' " +
            "WHERE n.relatedContract.id = :contractId " +
            "AND n.status = 'PENDING'")
    void cancelPendingNotificationsByContract(@Param("contractId") UUID contractId);
}