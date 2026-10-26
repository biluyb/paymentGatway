package com.safaricom.mpesa.service;

import com.safaricom.mpesa.dto.TransactionResponse;
import com.safaricom.mpesa.exception.ResourceNotFoundException;
import com.safaricom.mpesa.model.Account;
import com.safaricom.mpesa.model.Transaction;
import com.safaricom.mpesa.model.Transaction.TransactionStatus;
import com.safaricom.mpesa.repository.AccountRepository;
import com.safaricom.mpesa.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * TransactionServiceImpl
 *
 * <p>Implementation of {@link TransactionService} providing transaction queries
 * and compensating reversal transactions with balance restoration.</p>
 *
 * @author Bililign Yibgeta
 * @since 1.0.0
 */
@Service
public class TransactionServiceImpl implements TransactionService {

    private static final Logger log = LoggerFactory.getLogger(TransactionServiceImpl.class);

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    public TransactionServiceImpl(TransactionRepository transactionRepository, AccountRepository accountRepository) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponse> getAllTransactions() {
        log.debug("Fetching all transactions");
        return transactionRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public TransactionResponse getTransactionByReference(String reference) {
        log.debug("Fetching transaction by reference: {}", reference);
        Transaction tx = transactionRepository.findByTransactionReference(reference)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with reference: " + reference));
        return mapToResponse(tx);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public TransactionResponse reverseTransaction(String reference, String reason) {
        log.info("Initiating reversal for transaction: {}, reason: {}", reference, reason);

        Transaction tx = transactionRepository.findByTransactionReference(reference)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with reference: " + reference));

        if (tx.getStatus() != TransactionStatus.SUCCESS) {
            throw new IllegalArgumentException("Cannot reverse transaction with status: " + tx.getStatus());
        }

        // Credit the sender account back (compensating transaction)
        Account sender = accountRepository.findByAccountNumber(tx.getSenderAccount())
                .orElseThrow(() -> new ResourceNotFoundException("Sender account not found: " + tx.getSenderAccount()));

        sender.setBalance(sender.getBalance().add(tx.getAmount()));
        accountRepository.save(sender);

        tx.setStatus(TransactionStatus.REVERSED);
        tx.setFailureReason("Reversal executed: " + reason);
        Transaction savedTx = transactionRepository.save(tx);

        log.info("Transaction {} reversed successfully. Sender balance credited.", reference);
        return mapToResponse(savedTx);
    }

    private TransactionResponse mapToResponse(Transaction tx) {
        return new TransactionResponse(
                tx.getId(),
                tx.getTransactionReference(),
                tx.getSenderAccount(),
                tx.getReceiverAccount(),
                tx.getAmount(),
                tx.getCurrency(),
                tx.getStatus(),
                tx.getFailureReason(),
                tx.getCreatedAt()
        );
    }
}
