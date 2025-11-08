package com.rentas.properties.dao.repository;

import com.rentas.properties.dao.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    List<Notification> findByRecipientTypeAndRecipientId(String recipientType, UUID recipientId);

    List<Notification> findByStatus(String status);

    @Query("SELECT n FROM Notification n WHERE n.status = 'PENDING'")
    List<Notification> findPendingNotifications();

    @Query("SELECT n FROM Notification n WHERE n.status = 'FAILED'")
    List<Notification> findFailedNotifications();

    List<Notification> findByRelatedContractId(UUID contractId);

    List<Notification> findByRelatedPaymentId(UUID paymentId);
}