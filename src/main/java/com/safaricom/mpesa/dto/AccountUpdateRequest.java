package com.safaricom.mpesa.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * AccountUpdateRequest
 *
 * <p>Data Transfer Object for updating mutable account metadata.</p>
 *
 * @author Bililign Yibgeta
 * @since 1.0.0
 */
public class AccountUpdateRequest {

    @NotBlank(message = "Account holder name is required")
    @Size(min = 2, max = 100, message = "Account holder name must be between 2 and 100 characters")
    private String accountHolderName;

    public AccountUpdateRequest() {}

    public AccountUpdateRequest(String accountHolderName) {
        this.accountHolderName = accountHolderName;
    }

    public String getAccountHolderName() { return accountHolderName; }
    public void setAccountHolderName(String accountHolderName) { this.accountHolderName = accountHolderName; }
}
