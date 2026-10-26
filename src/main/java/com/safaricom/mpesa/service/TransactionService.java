package com.safaricom.mpesa.service;

import com.safaricom.mpesa.dto.TransactionResponse;

import java.util.List;

/**
 * TransactionService
 *
 * <p>Business service interface for querying transaction logs and executing financial reversals.</p>
 *
 * @author Bililign Yibgeta
 * @since 1.0.0
 */
public interface TransactionService {

    /**
     * Retrieves all recorded transactions across the bridge.
     *
     * @return list of transaction responses
     */
    List<TransactionResponse> getAllTransactions();

    /**
     * Retrieves a single transaction record by its unique reference code (e.g. MPESA...).
     *
     * @param reference unique transaction reference string
     * @return transaction response details
     */
    TransactionResponse getTransactionByReference(String reference);

    /**
     * Reverses a previously processed successful transaction and credits the sender account.
     *
     * @param reference transaction reference to reverse
     * @param reason administrative or compliance justification for the reversal
     * @return updated transaction record marked as REVERSED
     */
    TransactionResponse reverseTransaction(String reference, String reason);
}
