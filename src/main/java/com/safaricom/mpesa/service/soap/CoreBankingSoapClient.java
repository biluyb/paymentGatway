package com.safaricom.mpesa.service.soap;

import com.safaricom.mpesa.dto.soap.CoreBankingDebitRequest;
import com.safaricom.mpesa.dto.soap.CoreBankingDebitResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * CoreBankingSoapClient
 *
 * <p>Integration adapter and client for outbound XML/SOAP Web Services connecting
 * modern RESTful M-PESA channels to legacy Core Banking Systems (CBS) such as Temenos T24,
 * Finacle, or Oracle FLEXCUBE.</p>
 * <p>Special methods:</p>
 * <ul>
 *     <li>{@link #sendDebitInstruction(CoreBankingDebitRequest)}: Marshals debit payload, invokes CBS endpoint,
 *     and parses ISO 8583 response codes (00 Approved, 51 Insufficient Funds).</li>
 * </ul>
 *
 * @author Bililign Yibgeta
 * @since 1.0.0
 */
@Service
public class CoreBankingSoapClient {

    private static final Logger log = LoggerFactory.getLogger(CoreBankingSoapClient.class);

    /**
     * Simulates outbound SOAP WebService client dispatch to Core Banking.
     * In enterprise production deployment, this utilizes Spring {@code WebServiceTemplate}:
     * {@code (CoreBankingDebitResponse) webServiceTemplate.marshalSendAndReceive(uri, request);}
     *
     * @param request core banking debit parameters
     * @return response with response code (00 = success, 51 = insufficient funds) and core transaction reference
     */
    public CoreBankingDebitResponse sendDebitInstruction(CoreBankingDebitRequest request) {
        log.info("Dispatching SOAP XML Debit Request to Core Banking. Account: {}, Amount: {} {}",
                request.getBankAccountNumber(), request.getAmount(), request.getCurrency());

        // Account simulation: Accounts ending in "0000" simulate insufficient funds in CBS
        if (request.getBankAccountNumber() != null && request.getBankAccountNumber().endsWith("0000")) {
            log.warn("Core Banking SOAP returned error code: 51 (Insufficient Funds in CBS)");
            return new CoreBankingDebitResponse("51", "Insufficient Funds in Core Account", null);
        }

        String coreTxId = "CORE-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        log.info("Core Banking SOAP response received successfully: CoreTxId={}", coreTxId);
        return new CoreBankingDebitResponse("00", "Approved", coreTxId);
    }
}
