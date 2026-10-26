package com.safaricom.mpesa.service;

import com.safaricom.mpesa.dto.C2BPaymentRequest;
import com.safaricom.mpesa.dto.PaymentResponse;

/**
 * PaymentService
 *
 * <p>High-level orchestration service for financial payments across M-PESA mobile money
 * and backend core banking systems.</p>
 *
 * @author Bililign Yibgeta
 * @since 1.0.0
 */
public interface PaymentService {

    /**
     * Executes end-to-end customer-to-business (C2B) settlement.
     * Enforces idempotency, verifies sender funds, reserves transaction state, debits account,
     * and dispatches outbound SOAP debit instruction to the core banking ledger.
     *
     * @param idempotencyKey unique UUID identifier from client header
     * @param request validated payment parameters
     * @return PaymentResponse containing status and transaction reference
     */
    PaymentResponse processC2BPayment(String idempotencyKey, C2BPaymentRequest request);
}
