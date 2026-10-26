package com.safaricom.mpesa.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * AccountCreateRequest
 *
 * <p>Data Transfer Object representing incoming payload to register a new account on the gateway.</p>
 *
 * @author Bililign Yibgeta
 * @since 1.0.0
 */
public class AccountCreateRequest {

    @NotBlank(message = "Account number is required")
    @Pattern(regexp = "^(2517[0-9]{8}|2519[0-9]{8}|[0-9]{10,20})$",
            message = "Account number must be a valid Ethiopian MSISDN (e.g., 251712345678) or bank account number (10-20 digits)")
    private String accountNumber;

    @NotBlank(message = "Account holder name is required")
    @Size(min = 2, max = 100, message = "Account holder name must be between 2 and 100 characters")
    private String accountHolderName;

    @NotNull(message = "Initial balance is required")
    @DecimalMin(value = "0.00", message = "Initial balance cannot be negative")
    private BigDecimal initialBalance;

    @NotBlank(message = "Currency is required")
    @Pattern(regexp = "ETB", message = "Only ETB is currently supported")
    private String currency = "ETB";

    public AccountCreateRequest() {}

    public AccountCreateRequest(String accountNumber, String accountHolderName, BigDecimal initialBalance, String currency) {
        this.accountNumber = accountNumber;
        this.accountHolderName = accountHolderName;
        this.initialBalance = initialBalance;
        this.currency = currency;
    }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public String getAccountHolderName() { return accountHolderName; }
    public void setAccountHolderName(String accountHolderName) { this.accountHolderName = accountHolderName; }

    public BigDecimal getInitialBalance() { return initialBalance; }
    public void setInitialBalance(BigDecimal initialBalance) { this.initialBalance = initialBalance; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
}
