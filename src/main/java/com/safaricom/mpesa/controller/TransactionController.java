package com.safaricom.mpesa.controller;

import com.safaricom.mpesa.dto.TransactionResponse;
import com.safaricom.mpesa.service.TransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * TransactionController
 *
 * <p>REST API controller for inspecting transaction audit logs and initiating reversals.</p>
 *
 * <ul>
 *     <li>GET /api/v1/transactions - List all audit transactions</li>
 *     <li>GET /api/v1/transactions/{reference} - Lookup transaction status</li>
 *     <li>POST /api/v1/transactions/{reference}/reverse - Reverse transaction</li>
 * </ul>
 *
 * @author Bililign Yibgeta
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    /**
     * Retrieve all transaction history.
     *
     * @return 200 OK with list of transactions
     */
    @GetMapping
    public ResponseEntity<List<TransactionResponse>> getAllTransactions() {
        return ResponseEntity.ok(transactionService.getAllTransactions());
    }

    /**
     * Lookup transaction details by unique reference.
     *
     * @param reference MPESA transaction reference
     * @return 200 OK with transaction details
     */
    @GetMapping("/{reference}")
    public ResponseEntity<TransactionResponse> getTransactionByReference(@PathVariable String reference) {
        return ResponseEntity.ok(transactionService.getTransactionByReference(reference));
    }

    /**
     * Initiate a compensating transaction / reversal.
     *
     * @param reference MPESA transaction reference to reverse
     * @param payload optional body containing "reason"
     * @return 200 OK with updated REVERSED transaction
     */
    @PostMapping("/{reference}/reverse")
    public ResponseEntity<TransactionResponse> reverseTransaction(
            @PathVariable String reference,
            @RequestBody(required = false) Map<String, String> payload) {
        String reason = (payload != null && payload.containsKey("reason"))
                ? payload.get("reason")
                : "Customer disputed charge / administrative reversal";
        return ResponseEntity.ok(transactionService.reverseTransaction(reference, reason));
    }
}
