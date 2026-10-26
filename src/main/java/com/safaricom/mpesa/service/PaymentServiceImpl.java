package com.safaricom.mpesa.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.safaricom.mpesa.dto.C2BPaymentRequest;
import com.safaricom.mpesa.dto.PaymentResponse;
import com.safaricom.mpesa.dto.soap.CoreBankingDebitRequest;
import com.safaricom.mpesa.dto.soap.CoreBankingDebitResponse;
import com.safaricom.mpesa.exception.InsufficientFundsException;
import com.safaricom.mpesa.model.Account;
import com.safaricom.mpesa.model.IdempotencyRecord;
import com.safaricom.mpesa.model.Transaction;
import com.safaricom.mpesa.repository.AccountRepository;
import com.safaricom.mpesa.repository.IdempotencyRecordRepository;
import com.safaricom.mpesa.repository.TransactionRepository;
import com.safaricom.mpesa.service.soap.CoreBankingSoapClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * PaymentServiceImpl
 *
 * <p>Core orchestration implementation of {@link PaymentService}.</p>
 * <p>Transaction Workflow & Guarantees:</p>
 * <ol>
 *     <li><b>Idempotency Check</b>: Looks up {@link IdempotencyRecord} by {@code idempotencyKey}. If present, returns cached response immediately.</li>
 *     <li><b>Account Validation & Balance Check</b>: Queries sender {@link Account} and validates available funds.</li>
 *     <li><b>Transaction Ledger Creation</b>: Persists {@link Transaction} with initial {@code PENDING} status.</li>
 *     <li><b>Atomic Account Debit</b>: Deducts amount from sender account with optimistic concurrency locking.</li>
 *     <li><b>Core Banking SOAP Dispatch</b>: Sends XML/SOAP debit instruction to downstream legacy bank.</li>
 *     <li><b>Ledger Reconciliation & Idempotency Caching</b>: If SOAP succeeds, updates status to {@code SUCCESS}; otherwise rolls back and records {@code FAILED}.</li>
 * </ol>
 *
 * @author Bililign Yibgeta
 * @since 1.0.0
 */
@Service
public class PaymentServiceImpl implements PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentServiceImpl.class);

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final IdempotencyRecordRepository idempotencyRecordRepository;
    private final CoreBankingSoapClient soapClient;
    private final ObjectMapper objectMapper;

    /**
     * Injects dependencies for persistence, integration, and JSON serialization.
     */
    public PaymentServiceImpl(AccountRepository accountRepository,
                              TransactionRepository transactionRepository,
                              IdempotencyRecordRepository idempotencyRecordRepository,
                              CoreBankingSoapClient soapClient,
                              ObjectMapper objectMapper) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.idempotencyRecordRepository = idempotencyRecordRepository;
        this.soapClient = soapClient;
        this.objectMapper = objectMapper;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public PaymentResponse processC2BPayment(String idempotencyKey, C2BPaymentRequest request) {
        log.info("Processing C2B payment. IdempotencyKey: {}, Sender: {}, Amount: {}",
                idempotencyKey, request.getSenderMsisdn(), request.getAmount());

        // 1. Idempotency Check (prevent duplicate debit)
        Optional<IdempotencyRecord> existing = idempotencyRecordRepository.findById(idempotencyKey);
        if (existing.isPresent()) {
            log.info("Duplicate request detected. Returning cached response for key: {}", idempotencyKey);
            try {
                return objectMapper.readValue(existing.get().getResponsePayload(), PaymentResponse.class);
            } catch (Exception e) {
                log.error("Failed to deserialize cached response", e);
            }
        }

        // 2. Validate Sender Account
        Account sender = accountRepository.findByAccountNumber(request.getSenderMsisdn())
                .orElseThrow(() -> new IllegalArgumentException("Sender account not found: " + request.getSenderMsisdn()));

        if (sender.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientFundsException("Insufficient balance in account: " + sender.getAccountNumber());
        }

        // 3. Record Initial Transaction State
        String txRef = "MPESA" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        Transaction transaction = new Transaction();
        transaction.setTransactionReference(txRef);
        transaction.setSenderAccount(request.getSenderMsisdn());
        transaction.setReceiverAccount(request.getReceiverBankAccount());
        transaction.setAmount(request.getAmount());
        transaction.setCurrency(request.getCurrency());
        transaction.setStatus(Transaction.TransactionStatus.PENDING);
        transactionRepository.save(transaction);

        // 4. Debit Sender Balance (utilizes @Version optimistic locking)
        sender.setBalance(sender.getBalance().subtract(request.getAmount()));
        accountRepository.save(sender);

        // 5. Invoke Outbound Core Banking SOAP Service
        CoreBankingDebitRequest soapReq = new CoreBankingDebitRequest(
                request.getReceiverBankAccount(),
                request.getAmount(),
                request.getCurrency(),
                txRef
        );

        CoreBankingDebitResponse soapRes = soapClient.sendDebitInstruction(soapReq);

        PaymentResponse response;
        if (soapRes.isSuccessful()) {
            transaction.setStatus(Transaction.TransactionStatus.SUCCESS);
            transactionRepository.save(transaction);

            response = new PaymentResponse(
                    txRef,
                    "SUCCESS",
                    "Payment processed successfully",
                    soapRes.getCoreTransactionId(),
                    LocalDateTime.now()
            );
        } else {
            // Reversal on SOAP rejection
            sender.setBalance(sender.getBalance().add(request.getAmount()));
            accountRepository.save(sender);

            transaction.setStatus(Transaction.TransactionStatus.FAILED);
            transaction.setFailureReason("Core Banking rejected: " + soapRes.getResponseMessage());
            transactionRepository.save(transaction);

            response = new PaymentResponse(
                    txRef,
                    "FAILED",
                    "Core Banking rejected: " + soapRes.getResponseMessage(),
                    null,
                    LocalDateTime.now()
            );
        }

        // 6. Cache Idempotency Record
        try {
            String jsonPayload = objectMapper.writeValueAsString(response);
            idempotencyRecordRepository.save(new IdempotencyRecord(idempotencyKey, txRef, jsonPayload));
        } catch (Exception e) {
            log.error("Failed to persist idempotency record for key: {}", idempotencyKey, e);
        }

        return response;
    }
}
