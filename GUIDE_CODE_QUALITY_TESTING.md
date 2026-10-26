# Engineering Guide: Code Quality, Testing & Performance Benchmarking

## 1. Quality Standards in Financial Technology
In banking and financial integrations, code defects directly cause regulatory fines, financial loss, or reputational damage. Quality assurance operates across three layers:
1. **Static Analysis & Security Scanning (SonarQube)**: Prevents SQL injections, code smells, and hardcoded secrets.
2. **Automated Unit & Integration Testing (JUnit 5 & Mockito)**: Verifies business invariants and transactional rollbacks.
3. **Load & Stress Testing (Apache JMeter)**: Validates gateway throughput, latency percentiles (p95, p99), and idempotency concurrency.

---

## 2. Step-by-Step Implementation in this Project

### Step 1: Automated Unit Testing with JUnit 5 & Mockito
In [src/test/java/com/safaricom/mpesa/service/PaymentServiceTest.java](file:///d:/projects/safariintegration/src/test/java/com/safaricom/mpesa/service/PaymentServiceTest.java), we test core payment workflows using mock objects to simulate core banking and repositories:

```java
@Test
void processC2BPayment_Success() {
    // Arrange
    String idempotencyKey = UUID.randomUUID().toString();
    C2BPaymentRequest request = new C2BPaymentRequest(
        "251712345678", "1000123456789", new BigDecimal("500.00"), "ETB");

    Account account = new Account("251712345678", "Abebe Kebede", new BigDecimal("1000.00"), "ETB");
    when(accountRepository.findByAccountNumber(request.getSenderMsisdn())).thenReturn(Optional.of(account));
    when(soapClient.sendDebitInstruction(any())).thenReturn(new CoreBankingDebitResponse("00", "Approved", "CORE-12345"));

    // Act
    PaymentResponse response = paymentService.processC2BPayment(idempotencyKey, request);

    // Assert
    assertEquals("SUCCESS", response.getStatus());
    assertEquals(new BigDecimal("500.00"), account.getBalance()); // 1000 - 500 = 500
    verify(transactionRepository, times(2)).save(any(Transaction.class));
}
```

### Step 2: Testing Insufficient Funds Exceptions
```java
@Test
void processC2BPayment_ThrowsInsufficientFundsException() {
    String idempotencyKey = UUID.randomUUID().toString();
    C2BPaymentRequest request = new C2BPaymentRequest(
        "251712345678", "1000123456789", new BigDecimal("5000.00"), "ETB");

    Account account = new Account("251712345678", "Abebe Kebede", new BigDecimal("100.00"), "ETB");
    when(accountRepository.findByAccountNumber(request.getSenderMsisdn())).thenReturn(Optional.of(account));

    assertThrows(InsufficientFundsException.class, () -> {
        paymentService.processC2BPayment(idempotencyKey, request);
    });
}
```

---

### Step 3: SonarQube Code Quality & Security Auditing
The project enforces strict SonarQube quality gates integrated in [Jenkinsfile](file:///d:/projects/safariintegration/Jenkinsfile#L23-L29):
- **Bugs**: 0 allowed
- **Vulnerabilities**: 0 high or critical security vulnerabilities allowed
- **Security Hotspots**: 100% reviewed
- **Code Coverage**: Minimum 80% line and branch coverage
- **Duplication**: Under 3% across packages

---

### Step 4: High-Throughput Load Testing with Apache JMeter
To simulate peak transaction volumes (such as holiday sales or mobile money promotions), we created an Apache JMeter test plan:

- File: [jmeter/mpesa_c2b_load_test.jmx](file:///d:/projects/safariintegration/jmeter/mpesa_c2b_load_test.jmx)

#### JMeter Test Configuration:
- **Thread Group**: 200 concurrent user threads
- **Ramp-Up Period**: 30 seconds
- **Loop Count**: 100 iterations per thread (= 20,000 total payment requests)
- **Target Throughput**: ~1,000 Transactions Per Second (TPS)
- **Assertions**: HTTP Response Code Assertion (`200 OK`) and JSON Response assertion (`"status": "SUCCESS"`).
- **Dynamic Headers**: Generates unique `X-Idempotency-Key` via `${__UUID()}` and passes Bearer token.

#### How to Execute JMeter in CLI / Headless Mode:
```bash
jmeter -n -t jmeter/mpesa_c2b_load_test.jmx \
  -l target/jmeter_results.jtl \
  -e -o target/jmeter_report_html \
  -JAUTH_TOKEN="<PASTE_YOUR_JWT_TOKEN>"
```
This produces an interactive HTML report containing latency percentiles, response time graphs, error rate charts, and throughput summaries.
