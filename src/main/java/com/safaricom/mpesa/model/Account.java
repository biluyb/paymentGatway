package com.safaricom.mpesa.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * Account
 *
 * <p>JPA Entity representing a customer mobile wallet or bank settlement account.</p>
 * <p>Concurrency Control:</p>
 * <ul>
 *     <li>{@link #version}: Annotated with {@link Version} for optimistic concurrency locking.
 *     Prevents lost update anomalies in multi-threaded financial debits/credits without pessimistic table locks.</li>
 * </ul>
 *
 * @author Bililign Yibgeta
 * @since 1.0.0
 */
@Entity
@Table(name = "accounts", indexes = {
        @Index(name = "idx_acc_number", columnList = "accountNumber", unique = true)
})
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String accountNumber;

    @Column(nullable = false, length = 100)
    private String accountHolderName;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal balance;

    @Column(nullable = false, length = 3)
    private String currency;

    /**
     * Optimistic locking version field managed automatically by Hibernate.
     * Incremented on every UPDATE query to detect concurrent modifications.
     */
    @Version
    private Long version;

    public Account() {}

    public Account(String accountNumber, String accountHolderName, BigDecimal balance, String currency) {
        this.accountNumber = accountNumber;
        this.accountHolderName = accountHolderName;
        this.balance = balance;
        this.currency = currency;
    }

    public Long getId() { return id; }
    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
    public String getAccountHolderName() { return accountHolderName; }
    public void setAccountHolderName(String accountHolderName) { this.accountHolderName = accountHolderName; }
    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public Long getVersion() { return version; }
}
