package com.safaricom.mpesa.dto.soap;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "CoreBankingDebitResponse", namespace = "http://safaricom.com/corebanking")
@XmlAccessorType(XmlAccessType.FIELD)
public class CoreBankingDebitResponse {

    @XmlElement(required = true)
    private String responseCode;

    @XmlElement(required = true)
    private String responseMessage;

    @XmlElement
    private String coreTransactionId;

    public CoreBankingDebitResponse() {}

    public CoreBankingDebitResponse(String responseCode, String responseMessage, String coreTransactionId) {
        this.responseCode = responseCode;
        this.responseMessage = responseMessage;
        this.coreTransactionId = coreTransactionId;
    }

    public boolean isSuccessful() {
        return "00".equals(this.responseCode);
    }

    public String getResponseCode() { return responseCode; }
    public void setResponseCode(String responseCode) { this.responseCode = responseCode; }
    public String getResponseMessage() { return responseMessage; }
    public void setResponseMessage(String responseMessage) { this.responseMessage = responseMessage; }
    public String getCoreTransactionId() { return coreTransactionId; }
    public void setCoreTransactionId(String coreTransactionId) { this.coreTransactionId = coreTransactionId; }
}
