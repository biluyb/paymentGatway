package com.safaricom.mpesa.dto;

import com.safaricom.mpesa.model.Transaction.TransactionStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * TransactionResponse
 *
 * <p>Data Transfer Object returned by Transaction audit queries.</p>
 *
 * @author Bililign Yibgeta
 * @since 1.0.0
 */
public class TransactionResponse {

    private Long id;
    private String transactionReference;
    private String senderAccount;
    private String receiverAccount;
    private BigDecimal amount;
    private String currency;
    private TransactionStatus status;
    private String failureReason;
    private LocalDateTime createdAt;

    public TransactionResponse() {}

    public TransactionResponse(Long id, String transactionReference, String senderAccount,
                               String receiverAccount, BigDecimal amount, String currency,
                               TransactionStatus status, String failureReason, LocalDateTime createdAt) {
        this.id = id;
        this.transactionReference = transactionReference;
        this.senderAccount = senderAccount;
        this.receiverAccount = receiverAccount;
        this.amount = amount;
        this.currency = currency;
        this.status = status;
        this.failureReason = failureReason;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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
