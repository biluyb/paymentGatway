package com.safaricom.mpesa.controller;

import com.safaricom.mpesa.dto.AccountCreateRequest;
import com.safaricom.mpesa.dto.AccountResponse;
import com.safaricom.mpesa.dto.AccountUpdateRequest;
import com.safaricom.mpesa.dto.BalanceAdjustmentRequest;
import com.safaricom.mpesa.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AccountController
 *
 * <p>RESTful CRUD Controller managing M-PESA user and settlement accounts.
 * Protected by Spring Security and JWT authentication filter.</p>
 *
 * <ul>
 *     <li>POST /api/v1/accounts - Register new account</li>
 *     <li>GET /api/v1/accounts - List all accounts</li>
 *     <li>GET /api/v1/accounts/{id} - Get account by primary ID</li>
 *     <li>GET /api/v1/accounts/search - Query account by MSISDN</li>
 *     <li>PUT /api/v1/accounts/{id} - Update account details</li>
 *     <li>PATCH /api/v1/accounts/{id}/balance - Adjust account balance (Credit/Debit)</li>
 *     <li>DELETE /api/v1/accounts/{id} - Remove account</li>
 * </ul>
 *
 * @author Bililign Yibgeta
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    /**
     * Create/register a new financial account.
     *
     * @param request valid AccountCreateRequest payload
     * @return 201 CREATED with created account details
     */
    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(@Valid @RequestBody AccountCreateRequest request) {
        AccountResponse response = accountService.createAccount(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Retrieve all accounts.
     *
     * @return 200 OK with list of accounts
     */
    @GetMapping
    public ResponseEntity<List<AccountResponse>> getAllAccounts() {
        return ResponseEntity.ok(accountService.getAllAccounts());
    }

    /**
     * Retrieve an account by its database ID.
     *
     * @param id primary key identifier
     * @return 200 OK with account details
     */
    @GetMapping("/{id}")
    public ResponseEntity<AccountResponse> getAccountById(@PathVariable Long id) {
        return ResponseEntity.ok(accountService.getAccountById(id));
    }

    /**
     * Search an account by its unique MSISDN or bank account number.
     *
     * @param accountNumber MSISDN or bank account string
     * @return 200 OK with account details
     */
    @GetMapping("/search")
    public ResponseEntity<AccountResponse> getAccountByNumber(@RequestParam("accountNumber") String accountNumber) {
        return ResponseEntity.ok(accountService.getAccountByNumber(accountNumber));
    }

    /**
     * Update account details (such as account holder name).
     *
     * @param id primary key ID
     * @param request valid AccountUpdateRequest payload
     * @return 200 OK with updated account details
     */
    @PutMapping("/{id}")
    public ResponseEntity<AccountResponse> updateAccount(
            @PathVariable Long id,
            @Valid @RequestBody AccountUpdateRequest request) {
        return ResponseEntity.ok(accountService.updateAccount(id, request));
    }

    /**
     * Adjust account balance (Credit or Debit) with audit trail.
     *
     * @param id primary key ID
     * @param request valid BalanceAdjustmentRequest payload
     * @return 200 OK with refreshed account balance
     */
    @PatchMapping("/{id}/balance")
    public ResponseEntity<AccountResponse> adjustBalance(
            @PathVariable Long id,
            @Valid @RequestBody BalanceAdjustmentRequest request) {
        return ResponseEntity.ok(accountService.adjustBalance(id, request));
    }

    /**
     * Delete an account by its primary key ID.
     *
     * @param id primary key identifier
     * @return 204 NO_CONTENT on successful removal
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAccount(@PathVariable Long id) {
        accountService.deleteAccount(id);
        return ResponseEntity.noContent().build();
    }
}
