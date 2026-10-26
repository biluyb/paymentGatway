package com.safaricom.mpesa.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;

public class C2BPaymentRequest {

    @NotBlank(message = "Sender MSISDN is required")
    @Pattern(regexp = "^(2517[0-9]{8}|2519[0-9]{8})$", message = "Sender MSISDN must be valid Ethiopian phone number (e.g., 251712345678)")
    private String senderMsisdn;

    @NotBlank(message = "Receiver bank account is required")
    private String receiverBankAccount;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "1.00", message = "Minimum transaction amount is 1.00 ETB")
    private BigDecimal amount;

    @NotBlank(message = "Currency is required")
    @Pattern(regexp = "ETB", message = "Only ETB is currently supported")
    private String currency = "ETB";

    public C2BPaymentRequest() {}

    public C2BPaymentRequest(String senderMsisdn, String receiverBankAccount, BigDecimal amount, String currency) {
        this.senderMsisdn = senderMsisdn;
        this.receiverBankAccount = receiverBankAccount;
        this.amount = amount;
        this.currency = currency;
    }

    public String getSenderMsisdn() { return senderMsisdn; }
    public void setSenderMsisdn(String senderMsisdn) { this.senderMsisdn = senderMsisdn; }
    public String getReceiverBankAccount() { return receiverBankAccount; }
    public void setReceiverBankAccount(String receiverBankAccount) { this.receiverBankAccount = receiverBankAccount; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
}
