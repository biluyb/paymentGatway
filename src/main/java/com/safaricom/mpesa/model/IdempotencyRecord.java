package com.safaricom.mpesa.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * IdempotencyRecord
 *
 * <p>JPA Entity providing financial exactly-once execution guarantees (RFC 9110 / Stripe-style idempotency).
 * Stores the cached response for client-supplied {@code X-Idempotency-Key} headers to prevent double-debiting
 * during network retries or client retransmissions.</p>
 *
 * @author Bililign Yibgeta
 * @since 1.0.0
 */
@Entity
@Table(name = "idempotency_records")
public class IdempotencyRecord {

    @Id
    @Column(nullable = false, unique = true, length = 100)
    private String idempotencyKey;

    @Column(nullable = false, length = 64)
    private String transactionReference;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String responsePayload;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public IdempotencyRecord() {}

    public IdempotencyRecord(String idempotencyKey, String transactionReference, String responsePayload) {
        this.idempotencyKey = idempotencyKey;
        this.transactionReference = transactionReference;
        this.responsePayload = responsePayload;
        this.createdAt = LocalDateTime.now();
    }

    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
    public String getTransactionReference() { return transactionReference; }
    public void setTransactionReference(String transactionReference) { this.transactionReference = transactionReference; }
    public String getResponsePayload() { return responsePayload; }
    public void setResponsePayload(String responsePayload) { this.responsePayload = responsePayload; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
