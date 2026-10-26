package com.safaricom.mpesa.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Transaction
 *
 * <p>JPA Entity capturing the immutable financial audit log of every monetary
 * movement passing through the M-PESA integration bridge.</p>
 * <p>Special methods:</p>
 * <ul>
 *     <li>{@link #onCreate()}: JPA lifecycle {@code @PrePersist} hook guaranteeing timestamp immutability.</li>
 * </ul>
 *
 * @author Bililign Yibgeta
 * @since 1.0.0
 */
@Entity
@Table(name = "transactions", indexes = {
        @Index(name = "idx_tx_ref", columnList = "transactionReference"),
        @Index(name = "idx_tx_sender", columnList = "senderAccount")
})
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String transactionReference;

    @Column(nullable = false, length = 30)
    private String senderAccount;

    @Column(nullable = false, length = 30)
    private String receiverAccount;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionStatus status;

    @Column(length = 255)
    private String failureReason;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * JPA Entity Lifecycle Callback.
     * Automatically called immediately prior to database INSERT to stamp server creation time.
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Financial state lifecycle of an M-PESA transaction.
     */
    public enum TransactionStatus {
        /** Initial registered state prior to core banking dispatch */
        PENDING,
        /** Settlement acknowledged by core banking SOAP system */
        SUCCESS,
        /** Downstream timeout, communication error, or validation failure */
        FAILED,
        /** Reversal initiated to compensate sender funds */
        REVERSED
    }

    public Transaction() {}

    public Long getId() { return id; }
    public String getTransactionReference() { return transactionReference; }
    public void setTransactionReference(String transactionReference) { this.transactionReference = transactionReference; }
    public String getSenderAccount() { return senderAccount; }
    public void setSenderAccount(String senderAccount) { this.senderAccount = senderAccount; }
    public String getReceiverAccount() { return receiverAccount; }
    public void setReceiverAccount(String receiverAccount) { this.receiverAccount = receiverAccount; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public TransactionStatus getStatus() { return status; }
    public void setStatus(TransactionStatus status) { this.status = status; }
    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
