package com.safaricom.mpesa.service;

import com.safaricom.mpesa.dto.AccountCreateRequest;
import com.safaricom.mpesa.dto.AccountResponse;
import com.safaricom.mpesa.dto.AccountUpdateRequest;
import com.safaricom.mpesa.dto.BalanceAdjustmentRequest;
import com.safaricom.mpesa.exception.InsufficientFundsException;
import com.safaricom.mpesa.exception.ResourceNotFoundException;
import com.safaricom.mpesa.model.Account;
import com.safaricom.mpesa.repository.AccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * AccountServiceImpl
 *
 * <p>Implementation of {@link AccountService} enforcing ACID guarantees,
 * validation of uniqueness, and thread-safe balance operations using JPA versioning.</p>
 *
 * @author Bililign Yibgeta
 * @since 1.0.0
 */
@Service
public class AccountServiceImpl implements AccountService {

    private static final Logger log = LoggerFactory.getLogger(AccountServiceImpl.class);

    private final AccountRepository accountRepository;

    /**
     * Constructor injection for required repositories.
     *
     * @param accountRepository repository interface for accounts
     */
    public AccountServiceImpl(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public AccountResponse createAccount(AccountCreateRequest request) {
        log.info("Creating new account for holder: {}, number: {}", request.getAccountHolderName(), request.getAccountNumber());

        if (accountRepository.existsByAccountNumber(request.getAccountNumber())) {
            throw new IllegalArgumentException("Account already exists with number: " + request.getAccountNumber());
        }

        Account account = new Account(
                request.getAccountNumber(),
                request.getAccountHolderName(),
                request.getInitialBalance(),
                request.getCurrency()
        );

        Account saved = accountRepository.save(account);
        log.info("Account created successfully with ID: {}", saved.getId());
        return mapToResponse(saved);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public AccountResponse getAccountById(Long id) {
        log.debug("Fetching account by ID: {}", id);
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with ID: " + id));
        return mapToResponse(account);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public AccountResponse getAccountByNumber(String accountNumber) {
        log.debug("Fetching account by number: {}", accountNumber);
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with number: " + accountNumber));
        return mapToResponse(account);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<AccountResponse> getAllAccounts() {
        log.debug("Fetching all accounts");
        return accountRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public AccountResponse updateAccount(Long id, AccountUpdateRequest request) {
        log.info("Updating account metadata for ID: {}", id);
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with ID: " + id));

        account.setAccountHolderName(request.getAccountHolderName());
        Account updated = accountRepository.save(account);
        log.info("Account ID: {} updated successfully", id);
        return mapToResponse(updated);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public AccountResponse adjustBalance(Long id, BalanceAdjustmentRequest request) {
        log.info("Adjusting balance for account ID: {}, Type: {}, Amount: {}, Reason: {}",
                id, request.getType(), request.getAmount(), request.getReason());

        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with ID: " + id));

        if ("DEBIT".equalsIgnoreCase(request.getType())) {
            if (account.getBalance().compareTo(request.getAmount()) < 0) {
                throw new InsufficientFundsException("Insufficient funds for debit operation on account: " + account.getAccountNumber());
            }
            account.setBalance(account.getBalance().subtract(request.getAmount()));
        } else if ("CREDIT".equalsIgnoreCase(request.getType())) {
            account.setBalance(account.getBalance().add(request.getAmount()));
        } else {
            throw new IllegalArgumentException("Unsupported adjustment type: " + request.getType());
        }

        Account saved = accountRepository.save(account);
        log.info("Account ID: {} new balance: {}", id, saved.getBalance());
        return mapToResponse(saved);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public void deleteAccount(Long id) {
        log.info("Deleting account with ID: {}", id);
        if (!accountRepository.existsById(id)) {
            throw new ResourceNotFoundException("Account not found with ID: " + id);
        }
        accountRepository.deleteById(id);
        log.info("Account ID: {} deleted successfully", id);
    }

    /**
     * Helper mapper converting entity to DTO.
     *
     * @param account domain entity
     * @return response DTO
     */
    private AccountResponse mapToResponse(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getAccountNumber(),
                account.getAccountHolderName(),
                account.getBalance(),
                account.getCurrency(),
                account.getVersion()
        );
    }
}
