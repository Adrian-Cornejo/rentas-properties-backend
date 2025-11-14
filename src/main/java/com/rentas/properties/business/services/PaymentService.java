package com.rentas.properties.business.services;

import com.rentas.properties.api.dto.request.AddLateFeeRequest;
import com.rentas.properties.api.dto.request.CreatePaymentRequest;
import com.rentas.properties.api.dto.request.MarkAsPaidRequest;
import com.rentas.properties.api.dto.request.UpdatePaymentRequest;
import com.rentas.properties.api.dto.response.PaymentDetailResponse;
import com.rentas.properties.api.dto.response.PaymentResponse;
import com.rentas.properties.api.dto.response.PaymentSummaryResponse;

import java.util.List;
import java.util.UUID;

public interface PaymentService {

    PaymentDetailResponse createPayment(CreatePaymentRequest request);

    List<PaymentResponse> getAllPayments();

    PaymentDetailResponse getPaymentById(UUID id);

    PaymentDetailResponse updatePayment(UUID id, UpdatePaymentRequest request);

    PaymentDetailResponse markAsPaid(UUID id, MarkAsPaidRequest request);

    PaymentDetailResponse addLateFee(UUID id, AddLateFeeRequest request);

    int calculateAutomaticLateFees();

    List<PaymentResponse> getPaymentsByContract(UUID contractId);

    List<PaymentResponse> getPaymentsByStatus(String status);

    List<PaymentResponse> getPendingPayments();

    List<PaymentResponse> getOverduePayments();

    List<PaymentResponse> getPaymentsDueToday();

    List<PaymentResponse> getPaymentsByPeriod(int year, int month);

    PaymentSummaryResponse getPaymentsSummary();
}
