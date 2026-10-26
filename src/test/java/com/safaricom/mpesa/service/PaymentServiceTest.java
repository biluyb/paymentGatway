package com.safaricom.mpesa.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.safaricom.mpesa.dto.C2BPaymentRequest;
import com.safaricom.mpesa.dto.PaymentResponse;
import com.safaricom.mpesa.dto.soap.CoreBankingDebitResponse;
import com.safaricom.mpesa.exception.InsufficientFundsException;
import com.safaricom.mpesa.model.Account;
import com.safaricom.mpesa.model.Transaction;
import com.safaricom.mpesa.repository.AccountRepository;
import com.safaricom.mpesa.repository.IdempotencyRecordRepository;
import com.safaricom.mpesa.repository.TransactionRepository;
import com.safaricom.mpesa.service.soap.CoreBankingSoapClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private IdempotencyRecordRepository idempotencyRecordRepository;

    @Mock
    private CoreBankingSoapClient soapClient;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private Account testAccount;
    private C2BPaymentRequest testRequest;

    @BeforeEach
    void setUp() {
        testAccount = new Account("251700112233", "Abebe Kebede", new BigDecimal("1000.00"), "ETB");
        testRequest = new C2BPaymentRequest("251700112233", "1000123456", new BigDecimal("200.00"), "ETB");
    }

    @Test
    @DisplayName("Should successfully process C2B payment when balance is sufficient and SOAP approves")
    void testProcessPayment_Success() {
        // Arrange
        when(idempotencyRecordRepository.findById(any())).thenReturn(Optional.empty());
        when(accountRepository.findByAccountNumber("251700112233")).thenReturn(Optional.of(testAccount));
        when(soapClient.sendDebitInstruction(any())).thenReturn(new CoreBankingDebitResponse("00", "Approved", "CORE-999"));

        // Act
        PaymentResponse response = paymentService.processC2BPayment("KEY-12345", testRequest);

        // Assert
        assertNotNull(response);
        assertEquals("SUCCESS", response.getStatus());
        assertEquals(new BigDecimal("800.00"), testAccount.getBalance());
        verify(transactionRepository, atLeastOnce()).save(any(Transaction.class));
    }

    @Test
    @DisplayName("Should throw InsufficientFundsException when balance is too low")
    void testProcessPayment_InsufficientFunds() {
        // Arrange
        testAccount.setBalance(new BigDecimal("50.00"));
        when(idempotencyRecordRepository.findById(any())).thenReturn(Optional.empty());
        when(accountRepository.findByAccountNumber("251700112233")).thenReturn(Optional.of(testAccount));

        // Act & Assert
        assertThrows(InsufficientFundsException.class, () -> {
            paymentService.processC2BPayment("KEY-12345", testRequest);
        });

        verifyNoInteractions(soapClient);
    }
}
