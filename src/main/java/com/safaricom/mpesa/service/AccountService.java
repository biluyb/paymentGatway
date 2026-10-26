package com.safaricom.mpesa.service;

import com.safaricom.mpesa.dto.AccountCreateRequest;
import com.safaricom.mpesa.dto.AccountResponse;
import com.safaricom.mpesa.dto.AccountUpdateRequest;
import com.safaricom.mpesa.dto.BalanceAdjustmentRequest;

import java.util.List;

/**
 * AccountService
 *
 * <p>Business service interface for core Account lifecycle management,
 * supporting registration, retrieval, updating, balance adjustment, and deletion.</p>
 *
 * @author Bililign Yibgeta
 * @since 1.0.0
 */
public interface AccountService {

    /**
     * Registers a new account on the gateway.
     *
     * @param request valid account creation payload
     * @return created account details
     */
    AccountResponse createAccount(AccountCreateRequest request);

    /**
     * Retrieves an account by its unique primary key ID.
     *
     * @param id internal database primary key
     * @return account details
     */
    AccountResponse getAccountById(Long id);

    /**
     * Retrieves an account by its public identifier (MSISDN or bank account number).
     *
     * @param accountNumber unique account number
     * @return account details
     */
    AccountResponse getAccountByNumber(String accountNumber);

    /**
     * Retrieves all accounts registered on the bridge.
     *
     * @return list of account responses
     */
    List<AccountResponse> getAllAccounts();

    /**
     * Updates mutable profile metadata of an account.
     *
     * @param id primary key ID
     * @param request updated fields
     * @return updated account details
     */
    AccountResponse updateAccount(Long id, AccountUpdateRequest request);

    /**
     * Adjusts the balance of an existing account (debit/credit) with optimistic lock validation.
     *
     * @param id primary key ID
     * @param request adjustment payload
     * @return updated account details with fresh balance and version
     */
    AccountResponse adjustBalance(Long id, BalanceAdjustmentRequest request);

    /**
     * Deletes an account from the system.
     *
     * @param id primary key ID
     */
    void deleteAccount(Long id);
}
