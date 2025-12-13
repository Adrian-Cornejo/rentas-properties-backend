package com.rentas.properties.dao.repository;

import com.rentas.properties.dao.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    List<Payment> findByContractId(UUID contractId);

    List<Payment> findByStatus(String status);

    @Query("SELECT p FROM Payment p WHERE p.status = 'PENDIENTE'")
    List<Payment> findPendingPayments();

    @Query("SELECT p FROM Payment p WHERE p.status = 'ATRASADO' OR (p.status = 'PENDIENTE' AND p.dueDate < :today)")
    List<Payment> findOverduePayments(@Param("today") LocalDate today);

    @Query("SELECT p FROM Payment p WHERE p.dueDate = :today AND p.status = 'PENDIENTE'")
    List<Payment> findPaymentsDueToday(@Param("today") LocalDate today);

    List<Payment> findByPeriodYearAndPeriodMonth(Integer year, Integer month);

    @Query("SELECT p FROM Payment p WHERE p.contract.id = :contractId " +
            "AND p.periodYear = :year AND p.periodMonth = :month")
    Optional<Payment> findByContractAndPeriod(@Param("contractId") UUID contractId,
                                              @Param("year") Integer year,
                                              @Param("month") Integer month);

    @Query("SELECT p FROM Payment p WHERE p.contract.organization.id = :organizationId")
    List<Payment> findByOrganizationId(@Param("organizationId") UUID organizationId);

    @Query("SELECT p FROM Payment p WHERE p.contract.organization.id = :organizationId AND p.status = :status")
    List<Payment> findByOrganizationIdAndStatus(
            @Param("organizationId") UUID organizationId,
            @Param("status") String status
    );

    @Query("SELECT p FROM Payment p WHERE p.contract.organization.id = :organizationId AND p.status = 'PENDIENTE'")
    List<Payment> findPendingPaymentsByOrganization(@Param("organizationId") UUID organizationId);

    @Query("SELECT p FROM Payment p WHERE p.contract.organization.id = :organizationId " +
            "AND (p.status = 'ATRASADO' OR (p.status = 'PENDIENTE' AND p.dueDate < :today))")
    List<Payment> findOverduePaymentsByOrganization(
            @Param("organizationId") UUID organizationId,
            @Param("today") LocalDate today
    );

    @Query("SELECT p FROM Payment p WHERE p.contract.organization.id = :organizationId " +
            "AND p.dueDate = :today AND p.status = 'PENDIENTE'")
    List<Payment> findPaymentsDueTodayByOrganization(
            @Param("organizationId") UUID organizationId,
            @Param("today") LocalDate today
    );

    @Query("SELECT SUM(p.totalAmount) FROM Payment p " +
            "WHERE p.contract.organization.id = :organizationId AND p.status = 'PAGADO' " +
            "AND p.periodYear = :year AND p.periodMonth = :month")
    BigDecimal sumPaidAmountByOrganizationAndPeriod(
            @Param("organizationId") UUID organizationId,
            @Param("year") Integer year,
            @Param("month") Integer month
    );

    @Query("SELECT p FROM Payment p " +
            "WHERE p.dueDate = :dueDate " +
            "AND p.contract.organization.id = :organizationId " +
            "AND p.status IN ('PENDIENTE', 'ATRASADO')")
    List<Payment> findByDueDateAndOrganization(
            @Param("dueDate") LocalDate dueDate,
            @Param("organizationId") UUID organizationId
    );
}