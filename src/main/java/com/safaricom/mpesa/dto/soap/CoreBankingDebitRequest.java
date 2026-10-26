package com.safaricom.mpesa.dto.soap;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.math.BigDecimal;

@XmlRootElement(name = "CoreBankingDebitRequest", namespace = "http://safaricom.com/corebanking")
@XmlAccessorType(XmlAccessType.FIELD)
public class CoreBankingDebitRequest {

    @XmlElement(required = true)
    private String bankAccountNumber;

    @XmlElement(required = true)
    private BigDecimal amount;

    @XmlElement(required = true)
    private String currency;

    @XmlElement(required = true)
    private String reference;

    public CoreBankingDebitRequest() {}

    public CoreBankingDebitRequest(String bankAccountNumber, BigDecimal amount, String currency, String reference) {
        this.bankAccountNumber = bankAccountNumber;
        this.amount = amount;
        this.currency = currency;
        this.reference = reference;
    }

    public String getBankAccountNumber() { return bankAccountNumber; }
    public void setBankAccountNumber(String bankAccountNumber) { this.bankAccountNumber = bankAccountNumber; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }
}
