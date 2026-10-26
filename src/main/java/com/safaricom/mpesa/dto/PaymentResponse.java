package com.safaricom.mpesa.dto;

import java.time.LocalDateTime;

public class PaymentResponse {

    private String transactionReference;
    private String status;
    private String message;
    private String coreBankingTransactionId;
    private LocalDateTime timestamp;

    public PaymentResponse() {}

    public PaymentResponse(String transactionReference, String status, String message, String coreBankingTransactionId, LocalDateTime timestamp) {
        this.transactionReference = transactionReference;
        this.status = status;
        this.message = message;
        this.coreBankingTransactionId = coreBankingTransactionId;
        this.timestamp = timestamp;
    }

    public String getTransactionReference() { return transactionReference; }
    public void setTransactionReference(String transactionReference) { this.transactionReference = transactionReference; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getCoreBankingTransactionId() { return coreBankingTransactionId; }
    public void setCoreBankingTransactionId(String coreBankingTransactionId) { this.coreBankingTransactionId = coreBankingTransactionId; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
