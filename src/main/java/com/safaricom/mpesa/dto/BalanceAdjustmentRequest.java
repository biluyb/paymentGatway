package com.safaricom.mpesa.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

/**
 * BalanceAdjustmentRequest
 *
 * <p>Data Transfer Object for direct balance adjustments (e.g. deposit credit, manual adjustment)
 * with strict validation and audit reason tracking.</p>
 *
 * @author Bililign Yibgeta
 * @since 1.0.0
 */
public class BalanceAdjustmentRequest {

    @NotNull(message = "Adjustment amount is required")
    @DecimalMin(value = "0.01", message = "Minimum adjustment amount is 0.01")
    private BigDecimal amount;

    @NotBlank(message = "Adjustment type is required (CREDIT or DEBIT)")
    @Pattern(regexp = "^(CREDIT|DEBIT)$", message = "Adjustment type must be either CREDIT or DEBIT")
    private String type;

    @NotBlank(message = "Adjustment reason is required for regulatory audit trail")
    private String reason;

    public BalanceAdjustmentRequest() {}

    public BalanceAdjustmentRequest(BigDecimal amount, String type, String reason) {
        this.amount = amount;
        this.type = type;
        this.reason = reason;
    }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
