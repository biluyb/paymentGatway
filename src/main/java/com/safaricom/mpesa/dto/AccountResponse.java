package com.safaricom.mpesa.dto;

import java.math.BigDecimal;

/**
 * AccountResponse
 *
 * <p>Data Transfer Object returned by Account management endpoints.</p>
 *
 * @author Bililign Yibgeta
 * @since 1.0.0
 */
public class AccountResponse {

    private Long id;
    private String accountNumber;
    private String accountHolderName;
    private BigDecimal balance;
    private String currency;
    private Long version;

    public AccountResponse() {}

    public AccountResponse(Long id, String accountNumber, String accountHolderName, BigDecimal balance, String currency, Long version) {
        this.id = id;
        this.accountNumber = accountNumber;
        this.accountHolderName = accountHolderName;
        this.balance = balance;
        this.currency = currency;
        this.version = version;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public String getAccountHolderName() { return accountHolderName; }
    public void setAccountHolderName(String accountHolderName) { this.accountHolderName = accountHolderName; }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
}
