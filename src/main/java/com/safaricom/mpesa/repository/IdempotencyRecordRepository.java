package com.safaricom.mpesa.repository;

import com.safaricom.mpesa.model.IdempotencyRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * IdempotencyRecordRepository
 *
 * <p>Spring Data JPA repository accessing cached idempotent transaction payloads.</p>
 *
 * @author Bililign Yibgeta
 * @since 1.0.0
 */
@Repository
public interface IdempotencyRecordRepository extends JpaRepository<IdempotencyRecord, String> {
}
