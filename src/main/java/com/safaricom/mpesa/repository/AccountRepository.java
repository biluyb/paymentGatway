package com.safaricom.mpesa.repository;

import com.safaricom.mpesa.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * AccountRepository
 *
 * <p>Spring Data JPA repository interface for managing {@link Account} entities.
 * Supports standard CRUD operations, pagination, and custom query derivation.</p>
 *
 * @author Bililign Yibgeta
 * @since 1.0.0
 */
@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    /**
     * Finds an account by its unique MSISDN or bank account number.
     *
     * @param accountNumber unique account number string
     * @return Optional containing the Account if found, or empty
     */
    Optional<Account> findByAccountNumber(String accountNumber);

    /**
     * Checks if an account already exists with the specified account number.
     *
     * @param accountNumber account number to check
     * @return true if an account exists, false otherwise
     */
    boolean existsByAccountNumber(String accountNumber);
}
