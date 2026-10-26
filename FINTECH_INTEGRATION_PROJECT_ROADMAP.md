# M-PESA & Core Banking Integration Gateway Simulator (`mpesa-fintech-bridge`)
### Comprehensive Practical Roadmap & Technical Exam Guide for Safaricom Ethiopia

---

## 1. PROJECT OVERVIEW & ARCHITECTURE DESIGN

### The Real-World Scenario at Safaricom Ethiopia (M-PESA)
In telecom and banking integration, **M-PESA does not operate in a vacuum**. When an Ethiopian customer deposits cash at an agent, sends money from their bank to M-PESA (C2B), or transfers money from M-PESA to their bank account (B2C), M-PESA's integration layer acts as the **middleware gateway**:
1. **Inbound REST API:** Receives payment requests from mobile apps, USSD gateways, or merchant portals (JSON/REST).
2. **Security & Authentication:** Authenticates partners using OAuth2 / JWT and verifies cryptographic request signatures.
3. **Idempotency Gate:** Checks if the transaction reference was already received to prevent double-debiting.
4. **Outbound SOAP Client:** Communicates with legacy Core Banking Systems (e.g., Temenos T24, Flexcube, or Ethiopian National Payment Switch) over SOAP/XML.
5. **ACID Transaction Persistence:** Records pending, completed, or reversed transactions in PostgreSQL/MySQL.
6. **Reconciliation & Callback:** Dispatches webhook/callbacks with final transaction states.

```
+-----------------------------------------------------------------------------------+
|                           M-PESA INTEGRATION GATEWAY                              |
|                                                                                   |
|  [Mobile/Partner] --(REST/JSON + JWT)--> [REST Controller: /api/v1/mpesa]         |
|                                                     |                             |
|                                            [Validation & DTO]                     |
|                                                     |                             |
|                                          [Idempotency Check Filter]               |
|                                                     |                             |
|                                           [PaymentService]                        |
|                                            (@Transactional)                       |
|                                              /           \                        |
|                       [Spring Data JPA Repo]              [SOAP Core Banking]     |
|                                 |                             [Client (JAXB)]     |
|                                 v                                    |            |
|                     [(MySQL / PostgreSQL)]                           v            |
|                     - accounts table                         [Legacy Bank Core]   |
|                     - transactions table                     (Temenos / Soap WS)  |
|                     - idempotency_records                                         |
+-----------------------------------------------------------------------------------+
```

---

## 2. PROJECT REPOSITORY STRUCTURE (VS Code + Maven)

```text
mpesa-fintech-bridge/
├── pom.xml
├── Dockerfile
├── src/
│   ├── main/
│   │   ├── java/com/safaricom/mpesa/
│   │   │   ├── MpesaFintechApplication.java
│   │   │   ├── config/
│   │   │   │   ├── SecurityConfig.java
│   │   │   │   ├── JwtFilter.java
│   │   │   │   ├── SoapClientConfig.java
│   │   │   ├── controller/
│   │   │   │   ├── AuthController.java
│   │   │   │   └── PaymentController.java
│   │   │   ├── dto/
│   │   │   │   ├── AuthRequest.java
│   │   │   │   ├── AuthResponse.java
│   │   │   │   ├── C2BPaymentRequest.java
│   │   │   │   ├── PaymentResponse.java
│   │   │   │   └── soap/
│   │   │   │       ├── CoreBankingDebitRequest.java
│   │   │   │       └── CoreBankingDebitResponse.java
│   │   │   ├── exception/
│   │   │   │   ├── GlobalExceptionHandler.java
│   │   │   │   ├── InsufficientFundsException.java
│   │   │   │   └── DuplicateTransactionException.java
│   │   │   ├── model/
│   │   │   │   ├── Account.java
│   │   │   │   ├── Transaction.java
│   │   │   │   └── IdempotencyRecord.java
│   │   │   ├── repository/
│   │   │   │   ├── AccountRepository.java
│   │   │   │   ├── TransactionRepository.java
│   │   │   │   └── IdempotencyRecordRepository.java
│   │   │   └── service/
│   │   │       ├── PaymentService.java
│   │   │       ├── PaymentServiceImpl.java
│   │   │       ├── JwtService.java
│   │   │       └── soap/
│   │   │           └── CoreBankingSoapClient.java
│   │   └── resources/
│   │       ├── application.yml
│   │       └── wsdl/
│   │           └── corebanking.wsdl
│   └── test/
│       └── java/com/safaricom/mpesa/
│           ├── service/PaymentServiceTest.java
│           └── controller/PaymentControllerTest.java
```

---

## 3. STEP-BY-STEP BUILDING PATH

### Milestone 1: Project Setup & Maven Dependencies
**Why it matters in Fintech:** Safaricom requires modern Java (Java 17/21) with Spring Boot 3.x. Dependency management with Maven ensures enterprise builds are reproducible, secure, and compatible with CI/CD tools like Jenkins.

#### `pom.xml` Dependencies:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.3.4</version>
        <relativePath/>
    </parent>
    <groupId>com.safaricom</groupId>
    <artifactId>mpesa-fintech-bridge</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <name>mpesa-fintech-bridge</name>
    <description>M-PESA &amp; Core Banking Integration Gateway Simulator</description>

    <properties>
        <java.version>21</java.version>
        <jjwt.version>0.12.5</jjwt.version>
    </properties>

    <dependencies>
        <!-- Spring Boot Web for REST APIs -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <!-- Spring Data JPA for Database Persistence -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>

        <!-- Jakarta Validation (Strict financial input validation) -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>

        <!-- Spring Security & JWT for API Security -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-api</artifactId>
            <version>${jjwt.version}</version>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-impl</artifactId>
            <version>${jjwt.version}</version>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-jackson</artifactId>
            <version>${jjwt.version}</version>
            <scope>runtime</scope>
        </dependency>

        <!-- Spring Web Services (SOAP / XML for Core Banking) -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web-services</artifactId>
        </dependency>
        <dependency>
            <groupId>jakarta.xml.bind</groupId>
            <artifactId>jakarta.xml.bind-api</artifactId>
        </dependency>

        <!-- Database Drivers: MySQL (XAMPP) & PostgreSQL -->
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>

        <!-- Testing Dependencies -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.security</groupId>
            <artifactId>spring-security-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

#### `src/main/resources/application.yml`:
```yaml
server:
  port: 8080

spring:
  application:
    name: mpesa-fintech-bridge
  datasource:
    # MySQL via XAMPP (Port 3306)
    url: jdbc:mysql://localhost:3306/mpesa_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
    username: root
    password: ""
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        dialect: org.hibernate.dialect.MySQLDialect

jwt:
  secret: 404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
  expirationMs: 86400000 # 24 hours

fintech:
  soap:
    core-banking-url: http://localhost:8080/ws/corebanking
```

---

### Milestone 2: Domain Models & ACID Financial Transactions
**Why it matters in Fintech:** In financial transactions, float accuracy and ACID properties are non-negotiable.
- Never use `double` or `float` for money (rounding errors). Always use `BigDecimal`.
- Idempotency ensures that if an M-PESA user hits "Send" twice, or if a network drop causes a retry, they are charged **exactly once**.

#### 1. `model/Account.java`:
```java
package com.safaricom.mpesa.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "accounts")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String accountNumber; // e.g. "MSISDN:251712345678" or Bank Account

    @Column(nullable = false, length = 100)
    private String accountHolderName;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal balance;

    @Column(nullable = false, length = 3)
    private String currency; // "ETB"

    @Version
    private Long version; // Optimistic locking to prevent concurrent balance corruption

    public Account() {}

    public Account(String accountNumber, String accountHolderName, BigDecimal balance, String currency) {
        this.accountNumber = accountNumber;
        this.accountHolderName = accountHolderName;
        this.balance = balance;
        this.currency = currency;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
    public String getAccountHolderName() { return accountHolderName; }
    public void setAccountHolderName(String accountHolderName) { this.accountHolderName = accountHolderName; }
    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
}
```

#### 2. `model/Transaction.java`:
```java
package com.safaricom.mpesa.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String transactionReference; // e.g., "MPESA-ET-20261026-XYZ123"

    @Column(nullable = false)
    private String senderAccount;

    @Column(nullable = false)
    private String receiverAccount;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionStatus status; // PENDING, SUCCESS, FAILED, REVERSED

    private String failureReason;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public enum TransactionStatus {
        PENDING, SUCCESS, FAILED, REVERSED
    }

    // Getters and Setters
    public Long getId() { return id; }
    public String getTransactionReference() { return transactionReference; }
    public void setTransactionReference(String ref) { this.transactionReference = ref; }
    public String getSenderAccount() { return senderAccount; }
    public void setSenderAccount(String sender) { this.senderAccount = sender; }
    public String getReceiverAccount() { return receiverAccount; }
    public void setReceiverAccount(String receiver) { this.receiverAccount = receiver; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public TransactionStatus getStatus() { return status; }
    public void setStatus(TransactionStatus status) { this.status = status; }
    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String reason) { this.failureReason = reason; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
```

#### 3. `model/IdempotencyRecord.java`:
```java
package com.safaricom.mpesa.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "idempotency_records")
public class IdempotencyRecord {

    @Id
    @Column(nullable = false, unique = true, length = 100)
    private String idempotencyKey; // Provided by client in HTTP Header: X-Idempotency-Key

    @Column(nullable = false, length = 64)
    private String transactionReference;

    @Column(nullable = false)
    private String responsePayload;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public IdempotencyRecord() {}

    public IdempotencyRecord(String idempotencyKey, String transactionReference, String responsePayload) {
        this.idempotencyKey = idempotencyKey;
        this.transactionReference = transactionReference;
        this.responsePayload = responsePayload;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public String getIdempotencyKey() { return idempotencyKey; }
    public String getTransactionReference() { return transactionReference; }
    public String getResponsePayload() { return responsePayload; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
```

---

### Milestone 3: SOAP Integration Layer (Core Banking Integration)
**Why it matters in Fintech:** Many established banks (such as Tsehay Bank, Commercial Bank of Ethiopia, Awash) expose **SOAP/XML Web Services** for their Core Banking (Temenos T24, Oracle Flexcube). As a Fintech Integration Engineer at Safaricom, you must prove you can marshall/unmarshall XML, call external SOAP endpoints, and handle timeouts.

#### DTOs for XML Marshalling (`dto/soap/CoreBankingDebitRequest.java`):
```java
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

    // Getters and Setters
    public String getBankAccountNumber() { return bankAccountNumber; }
    public void setBankAccountNumber(String bankAccountNumber) { this.bankAccountNumber = bankAccountNumber; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }
}
```

#### SOAP Response (`dto/soap/CoreBankingDebitResponse.java`):
```java
package com.safaricom.mpesa.dto.soap;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "CoreBankingDebitResponse", namespace = "http://safaricom.com/corebanking")
@XmlAccessorType(XmlAccessType.FIELD)
public class CoreBankingDebitResponse {

    @XmlElement(required = true)
    private String responseCode; // "00" = SUCCESS, "51" = INSUFFICIENT_FUNDS

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

    // Getters and Setters
    public String getResponseCode() { return responseCode; }
    public void setResponseCode(String responseCode) { this.responseCode = responseCode; }
    public String getResponseMessage() { return responseMessage; }
    public void setResponseMessage(String responseMessage) { this.responseMessage = responseMessage; }
    public String getCoreTransactionId() { return coreTransactionId; }
    public void setCoreTransactionId(String coreTransactionId) { this.coreTransactionId = coreTransactionId; }
}
```

#### SOAP Client Service (`service/soap/CoreBankingSoapClient.java`):
```java
package com.safaricom.mpesa.service.soap;

import com.safaricom.mpesa.dto.soap.CoreBankingDebitRequest;
import com.safaricom.mpesa.dto.soap.CoreBankingDebitResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CoreBankingSoapClient {

    private static final Logger log = LoggerFactory.getLogger(CoreBankingSoapClient.class);

    /**
     * In a live enterprise integration, this method uses Spring WebServiceTemplate:
     * (CoreBankingDebitResponse) webServiceTemplate.marshalSendAndReceive(uri, request);
     * Here, it simulates the real SOAP XML call with bank response simulation.
     */
    public CoreBankingDebitResponse sendDebitInstruction(CoreBankingDebitRequest request) {
        log.info("Dispatching SOAP XML Debit Request to Core Banking. Account: {}, Amount: {} {}",
                request.getBankAccountNumber(), request.getAmount(), request.getCurrency());

        // Simulating core banking validation
        if (request.getBankAccountNumber().endsWith("0000")) {
            log.warn("Core Banking SOAP returned error code: 51 (Insufficient Funds)");
            return new CoreBankingDebitResponse("51", "Insufficient Funds in Core Account", null);
        }

        String coreTxId = "CORE-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        log.info("Core Banking SOAP response received successfully: CoreTxId={}", coreTxId);
        return new CoreBankingDebitResponse("00", "Approved", coreTxId);
    }
}
```

---

### Milestone 4: Payment Service with ACID `@Transactional` & Idempotency
**Why it matters in Fintech:** Interviewers *always* grill candidates on:
1. "What happens if a transaction fails halfway through?" -> `@Transactional(rollbackFor = Exception.class)` rolls back DB changes.
2. "What if the network times out during the external bank call?" -> Mark transaction as `PENDING`, log the event, and provide a reconciliation job.

#### `service/PaymentServiceImpl.java`:
```java
package com.safaricom.mpesa.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.safaricom.mpesa.dto.C2BPaymentRequest;
import com.safaricom.mpesa.dto.PaymentResponse;
import com.safaricom.mpesa.dto.soap.CoreBankingDebitRequest;
import com.safaricom.mpesa.dto.soap.CoreBankingDebitResponse;
import com.safaricom.mpesa.exception.InsufficientFundsException;
import com.safaricom.mpesa.model.Account;
import com.safaricom.mpesa.model.IdempotencyRecord;
import com.safaricom.mpesa.model.Transaction;
import com.safaricom.mpesa.repository.AccountRepository;
import com.safaricom.mpesa.repository.IdempotencyRecordRepository;
import com.safaricom.mpesa.repository.TransactionRepository;
import com.safaricom.mpesa.service.soap.CoreBankingSoapClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class PaymentServiceImpl implements PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentServiceImpl.class);

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final IdempotencyRecordRepository idempotencyRecordRepository;
    private final CoreBankingSoapClient soapClient;
    private final ObjectMapper objectMapper;

    public PaymentServiceImpl(AccountRepository accountRepository,
                              TransactionRepository transactionRepository,
                              IdempotencyRecordRepository idempotencyRecordRepository,
                              CoreBankingSoapClient soapClient,
                              ObjectMapper objectMapper) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.idempotencyRecordRepository = idempotencyRecordRepository;
        this.soapClient = soapClient;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public PaymentResponse processC2BPayment(String idempotencyKey, C2BPaymentRequest request) {
        log.info("Processing C2B payment. IdempotencyKey: {}, Sender: {}, Amount: {}",
                idempotencyKey, request.getSenderMsisdn(), request.getAmount());

        // 1. IDEMPOTENCY CHECK: Return existing response if already processed
        Optional<IdempotencyRecord> existing = idempotencyRecordRepository.findById(idempotencyKey);
        if (existing.isPresent()) {
            log.info("Duplicate request detected. Returning cached response for key: {}", idempotencyKey);
            try {
                return objectMapper.readValue(existing.get().getResponsePayload(), PaymentResponse.class);
            } catch (Exception e) {
                log.error("Failed to deserialize cached response", e);
            }
        }

        // 2. Validate Sender Account
        Account sender = accountRepository.findByAccountNumber(request.getSenderMsisdn())
                .orElseThrow(() -> new IllegalArgumentException("Sender account not found: " + request.getSenderMsisdn()));

        if (sender.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientFundsException("Insufficient balance in account: " + sender.getAccountNumber());
        }

        // 3. Create Transaction in PENDING state
        String txRef = "MPESA" + System.currentTimeMillis();
        Transaction transaction = new Transaction();
        transaction.setTransactionReference(txRef);
        transaction.setSenderAccount(request.getSenderMsisdn());
        transaction.setReceiverAccount(request.getReceiverBankAccount());
        transaction.setAmount(request.getAmount());
        transaction.setCurrency(request.getCurrency());
        transaction.setStatus(Transaction.TransactionStatus.PENDING);
        transactionRepository.save(transaction);

        // 4. Debit Local M-PESA Wallet
        sender.setBalance(sender.getBalance().subtract(request.getAmount()));
        accountRepository.save(sender);

        // 5. Invoke Core Banking SOAP Integration
        CoreBankingDebitRequest soapReq = new CoreBankingDebitRequest(
                request.getReceiverBankAccount(),
                request.getAmount(),
                request.getCurrency(),
                txRef
        );

        CoreBankingDebitResponse soapRes = soapClient.sendDebitInstruction(soapReq);

        PaymentResponse response;
        if (soapRes.isSuccessful()) {
            transaction.setStatus(Transaction.TransactionStatus.SUCCESS);
            transactionRepository.save(transaction);

            response = new PaymentResponse(
                    txRef,
                    "SUCCESS",
                    "Payment processed successfully",
                    soapRes.getCoreTransactionId(),
                    LocalDateTime.now()
            );
        } else {
            // Business failure at Core Banking -> Rollback wallet & mark FAILED
            transaction.setStatus(Transaction.TransactionStatus.FAILED);
            transaction.setFailureReason(soapRes.getResponseMessage());
            transactionRepository.save(transaction);

            // Re-credit the wallet
            sender.setBalance(sender.getBalance().add(request.getAmount()));
            accountRepository.save(sender);

            response = new PaymentResponse(
                    txRef,
                    "FAILED",
                    "Bank rejected transaction: " + soapRes.getResponseMessage(),
                    null,
                    LocalDateTime.now()
            );
        }

        // 6. Save Idempotency Record for subsequent retries
        try {
            String payload = objectMapper.writeValueAsString(response);
            idempotencyRecordRepository.save(new IdempotencyRecord(idempotencyKey, txRef, payload));
        } catch (Exception ex) {
            log.error("Could not persist idempotency payload", ex);
        }

        return response;
    }
}
```

---

### Milestone 5: REST Controller, Validation & Centralized Error Handling
**Why it matters in Fintech:** Fintech APIs must follow strict input validation (e.g. MSISDN formats `2517...` / `2519...`, positive non-zero amounts) and return consistent error structures (such as RFC 7807 `ProblemDetail`).

#### `controller/PaymentController.java`:
```java
package com.safaricom.mpesa.controller;

import com.safaricom.mpesa.dto.C2BPaymentRequest;
import com.safaricom.mpesa.dto.PaymentResponse;
import com.safaricom.mpesa.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/mpesa/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/c2b")
    public ResponseEntity<PaymentResponse> initiateC2BPayment(
            @RequestHeader(value = "X-Idempotency-Key", required = true) String idempotencyKey,
            @Valid @RequestBody C2BPaymentRequest request) {

        PaymentResponse response = paymentService.processC2BPayment(idempotencyKey, request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
```

#### Centralized Exception Handling (`exception/GlobalExceptionHandler.java`):
```java
package com.safaricom.mpesa.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InsufficientFundsException.class)
    public ProblemDetail handleInsufficientFunds(InsufficientFundsException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problem.setTitle("Insufficient Funds");
        problem.setType(URI.create("https://developer.safaricom.et/errors/insufficient-funds"));
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationErrors(MethodArgumentNotValidException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation failed");
        problem.setTitle("Invalid Request Payload");

        Map<String, String> errors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }
        problem.setProperty("fieldErrors", errors);
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenericException(Exception ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "An internal error occurred. Please contact M-PESA support.");
        problem.setTitle("Internal Server Error");
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }
}
```

---

### Milestone 6: Unit & Integration Testing (JUnit 5 & Mockito)
**Why it matters in Fintech:** Safaricom places strong weight on code quality, automated testing, and SonarQube test coverage. You must demonstrate how to test services with mocked dependencies without needing a live bank.

#### `src/test/java/com/safaricom/mpesa/service/PaymentServiceTest.java`:
```java
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
        testAccount.setBalance(new BigDecimal("50.00")); // Lower than requested 200.00
        when(idempotencyRecordRepository.findById(any())).thenReturn(Optional.empty());
        when(accountRepository.findByAccountNumber("251700112233")).thenReturn(Optional.of(testAccount));

        // Act & Assert
        assertThrows(InsufficientFundsException.class, () -> {
            paymentService.processC2BPayment("KEY-12345", testRequest);
        });

        verifyNoInteractions(soapClient);
    }
}
```

---

## 4. DOCKER & SONARQUBE READINESS (CI/CD)

### Production Multi-Stage `Dockerfile`:
```dockerfile
# Stage 1: Build JAR using Maven
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Minimal, secure JRE runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S mpesa && adduser -S mpesa -G mpesa
USER mpesa

COPY --from=builder /app/target/mpesa-fintech-bridge-1.0.0-SNAPSHOT.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-XX:+UseG1GC", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
```

---

## 5. INTERVIEW & EXAM CHEAT SHEET: HOW TO DEFEND YOUR CODE

When the Safaricom technical interview panel asks questions, here are your direct, high-scoring answers:

| Question / Topic | What Safaricom Panel Expects | How You Answer Based on Your Project |
| :--- | :--- | :--- |
| **Why not use `double` for currency?** | Precision & IEEE 754 floating-point errors. | "In financial software, `double` causes binary rounding inaccuracies. I always use `BigDecimal` with explicit scale (`precision = 19, scale = 4`) and `RoundingMode.HALF_EVEN`." |
| **How do you handle Idempotency in M-PESA APIs?** | Preventing double-charges from network retries. | "I enforce a client-provided `X-Idempotency-Key`. Before processing, I check `idempotency_records`. If already present, I immediately return the cached response without debiting again." |
| **How does `@Transactional` guarantee consistency?** | ACID properties & isolation levels. | "I use `@Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)` with optimistic locking (`@Version`) so concurrent requests do not corrupt account balance." |
| **How do you bridge REST and SOAP in telecom?** | Protocol conversion & XML marshalling. | "M-PESA client apps communicate via JSON REST, while legacy core banking systems communicate via SOAP/XML. I build a Spring Boot adapter that marshalls Java DTOs to SOAP XML using JAXB, handles timeouts gracefully, and returns clean REST responses." |
| **How do you pass SonarQube Quality Gates?** | Clean code & zero critical vulnerabilities. | "I keep cognitive complexity below 15, eliminate hardcoded credentials by using environment variables / Spring Cloud Config, avoid catching raw `Throwable`, write unit tests covering >80% logic with JUnit 5/Mockito, and use SLF4J logging without exposing PII (Personally Identifiable Information)." |
| **What happens if the Bank Core is completely down?** | Circuit breaker & graceful degradation. | "I wrap external SOAP calls in Resilience4j CircuitBreakers (`@CircuitBreaker`). After N consecutive connection timeouts, the circuit OPENS immediately rejecting further calls fast with a 503 Service Unavailable, preventing thread exhaustion in our gateway." |
| **How does Safaricom M-PESA verify message integrity?** | HMAC SHA256 / Digital Signature. | "In Safaricom M-PESA B2C/C2B integrations, requests are signed using an HMAC-SHA256 signature calculated over the payload using a shared Secret/Initiator password. I verify this signature in a Spring filter before deserializing the request." |
| **How do you handle asynchronous transaction settlement?** | Message Queues & Event-Driven Architecture. | "For high-volume operations like bulk merchant payouts, the REST API validates the input, publishes an event to Apache Kafka or RabbitMQ, and returns HTTP 202 Accepted. Consumer worker microservices process the transactions asynchronously." |
| **Optimistic vs Pessimistic Locking in Banking?** | Managing concurrent debits on one account. | "Optimistic locking (`@Version`) is optimal for high-throughput reads with moderate writes, failing fast if a concurrent update occurs. For sensitive settlement accounts with heavy concurrent debits, I use Pessimistic Write Locking (`@Lock(LockModeType.PESSIMISTIC_WRITE)`) to serialize balance updates at the database row level." |

---

## 6. THE 24-HOUR FINAL EXAM PREPARATION CHECKLIST

With only 1 day to prepare for Safaricom Ethiopia's technical exam, follow this focused timeline:

### Phase 1 (Morning): Master the Core API & Transaction Flow
- [ ] Understand why `@Transactional(rollbackFor = Exception.class)` is mandatory.
- [ ] Memorize the 4 ACID properties and how relational databases (MySQL/PostgreSQL/Oracle) enforce them:
  - **Atomicity:** All sub-steps (wallet debit + record creation) succeed or all roll back.
  - **Consistency:** Account balances never violate constraints (no negative balances, foreign keys intact).
  - **Isolation:** Preventing dirty reads using `READ_COMMITTED` or `SERIALIZABLE`.
  - **Durability:** Committed transactions are flushed to the Write-Ahead Log (WAL) on disk.
- [ ] Walk through [PaymentServiceImpl.java](file:///d:/projects/safariintegration/src/main/java/com/safaricom/mpesa/service/PaymentServiceImpl.java) line-by-line.

### Phase 2 (Afternoon): SOAP, REST & Security Fundamentals
- [ ] Know the difference between **REST** (HTTP verbs, JSON, lightweight) and **SOAP** (XML envelope, WSDL contract, strict schema, WS-Security).
- [ ] Review how JAXB annotations work (`@XmlRootElement`, `@XmlElement`).
- [ ] Review how JWT stateless authentication functions in [JwtFilter.java](file:///d:/projects/safariintegration/src/main/java/com/safaricom/mpesa/config/JwtFilter.java) using the `Authorization: Bearer <token>` header.
- [ ] Review HTTP Status Codes used in fintech:
  - `200 OK` (Processed successfully)
  - `201 Created` (Account or transaction initiated)
  - `400 Bad Request` (Validation error or insufficient funds)
  - `401 Unauthorized` (Invalid or missing JWT token)
  - `409 Conflict` (Duplicate transaction detected via Idempotency)
  - `500 Internal Server Error` (Unhandled runtime crash)
  - `503 Service Unavailable` (Bank partner connection down)

### Phase 3 (Evening): Unit Testing & CI/CD
- [ ] Inspect [PaymentServiceTest.java](file:///d:/projects/safariintegration/src/test/java/com/safaricom/mpesa/service/PaymentServiceTest.java).
- [ ] Practice explaining how `@Mock`, `@InjectMocks`, and `Mockito.when(...).thenReturn(...)` enable testing banking services without calling real databases or external bank APIs.
- [ ] Understand the 2-stage [Dockerfile](file:///d:/projects/safariintegration/Dockerfile):
  - Stage 1 compiles using Maven.
  - Stage 2 copies only the `.jar` into a lightweight JRE Alpine image and runs as a non-root user for security.

### Phase 4 (Night): Live Code Defense (Your Tsehay Bank Story)
When the panel asks: *"Tell us about yourself and your integration experience"*:
> *"I am a Software Engineer specializing in Fintech and Core Banking Integration. At Tsehay Bank, I engineered middleware connecting our Temenos Core Banking environment with external payment platforms like Telebirr and the national FAYDA ID harmonization platform using Java, Spring Boot, SOAP web services, and Oracle SQL. I ensure high-volume integrations remain secure, idempotent, and resilient against network timeouts while meeting strict SonarQube quality gates. Joining Safaricom Ethiopia's M-PESA team is the natural next step where I can bring my direct banking integration experience to scale digital financial inclusion."*

---

## 7. READY-TO-RUN COMMANDS IN VS CODE

Open your integrated terminal in VS Code (`Ctrl + ` `) and run:

```bash
# 1. Run all Unit Tests (verifies compilation, Mockito, and JUnit 5)
mvn clean test

# 2. Package into a production JAR
mvn clean package -DskipTests

# 3. Run the Spring Boot application locally
java -jar target/mpesa-fintech-bridge-1.0.0-SNAPSHOT.jar
```

