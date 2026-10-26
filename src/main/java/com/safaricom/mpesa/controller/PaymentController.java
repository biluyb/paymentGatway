package com.safaricom.mpesa.controller;

import com.safaricom.mpesa.dto.C2BPaymentRequest;
import com.safaricom.mpesa.dto.PaymentResponse;
import com.safaricom.mpesa.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * PaymentController
 *
 * <p>Financial transaction gateway controller exposing customer-to-business (C2B)
 * payment processing endpoints with idempotency enforcement.</p>
 *
 * <ul>
 *     <li>POST /api/v1/mpesa/payments/c2b - Process C2B mobile money transaction</li>
 * </ul>
 *
 * @author Bililign Yibgeta
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/mpesa/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * Initiates a C2B payment debit instruction against the sender's M-PESA mobile account,
     * guarantees idempotent execution via {@code X-Idempotency-Key}, and triggers core banking SOAP dispatch.
     *
     * @param idempotencyKey unique UUID header preventing duplicate debit processing
     * @param request validated financial payment request payload
     * @return 200 OK with PaymentResponse containing unique reference and transaction status
     */
    @PostMapping("/c2b")
    public ResponseEntity<PaymentResponse> initiateC2BPayment(
            @RequestHeader(value = "X-Idempotency-Key", required = true) String idempotencyKey,
            @Valid @RequestBody C2BPaymentRequest request) {

        PaymentResponse response = paymentService.processC2BPayment(idempotencyKey, request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
