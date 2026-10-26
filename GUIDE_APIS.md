# Engineering Guide: Financial APIs & Secure Interfaces

## 1. Financial API Standards & Principles
In financial engineering, APIs are contracts between regulated entities (central banks, telecom operators, core banking systems, merchant aggregators). Unlike casual web APIs, financial APIs must enforce:
1. **Zero-Trust Security**: No unauthenticated endpoints except the token generation gateway.
2. **Strict Field Validation**: Rejection of malformed account numbers, negative amounts, or unsupported currencies before hitting the database.
3. **RFC 9110 Idempotency**: Prevention of duplicate debit instructions during mobile network retries.
4. **RFC 7807 Standardized Errors**: Consistent problem details returned to API consumers.

---

## 2. Step-by-Step Implementation in this Project

### Step 1: Enforcing Cryptographic Zero-Trust Authentication
Every API request must present a signed Bearer token verified against HMAC-SHA256 secrets.

In [JwtFilter.java](file:///d:/projects/safariintegration/src/main/java/com/safaricom/mpesa/config/JwtFilter.java#L34-L57):
```java
final String authHeader = request.getHeader("Authorization");
if (authHeader == null || !authHeader.startsWith("Bearer ")) {
    filterChain.doFilter(request, response);
    return;
}

String jwt = authHeader.substring(7);
String username = jwtService.extractUsername(jwt);

if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
    if (jwtService.validateToken(jwt, username)) {
        UsernamePasswordAuthenticationToken authToken = 
            new UsernamePasswordAuthenticationToken(username, null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }
}
filterChain.doFilter(request, response);
```

### Step 2: Strict Input Validation on Financial DTOs
Incoming JSON payloads must be validated at the controller boundary using Jakarta Bean Validation.

In [C2BPaymentRequest.java](file:///d:/projects/safariintegration/src/main/java/com/safaricom/mpesa/dto/C2BPaymentRequest.java#L11-L24):
```java
@NotBlank(message = "Sender MSISDN is required")
@Pattern(regexp = "^(2517[0-9]{8}|2519[0-9]{8})$", 
         message = "Sender MSISDN must be valid Ethiopian phone number (e.g., 251712345678)")
private String senderMsisdn;

@NotBlank(message = "Receiver bank account is required")
private String receiverBankAccount;

@NotNull(message = "Amount is required")
@DecimalMin(value = "1.00", message = "Minimum transaction amount is 1.00 ETB")
private BigDecimal amount;

@NotBlank(message = "Currency is required")
@Pattern(regexp = "ETB", message = "Only ETB is currently supported")
private String currency = "ETB";
```

### Step 3: Enforcing Client Idempotency via Header
Financial payments cannot be retried blindly. We demand the client send an `X-Idempotency-Key` UUID header.

In [PaymentController.java](file:///d:/projects/safariintegration/src/main/java/com/safaricom/mpesa/controller/PaymentController.java#L22-L28):
```java
@PostMapping("/c2b")
public ResponseEntity<PaymentResponse> initiateC2BPayment(
        @RequestHeader(value = "X-Idempotency-Key", required = true) String idempotencyKey,
        @Valid @RequestBody C2BPaymentRequest request) {

    PaymentResponse response = paymentService.processC2BPayment(idempotencyKey, request);
    return new ResponseEntity<>(response, HttpStatus.OK);
}
```

### Step 4: Full CRUD Operations for Accounts
The [AccountController.java](file:///d:/projects/safariintegration/src/main/java/com/safaricom/mpesa/controller/AccountController.java) exposes full RESTful lifecycle methods:
- `POST /api/v1/accounts`: Registers new account with `201 CREATED`.
- `GET /api/v1/accounts`: Returns all accounts.
- `GET /api/v1/accounts/{id}`: Looks up account by ID.
- `GET /api/v1/accounts/search?accountNumber=...`: Looks up account by MSISDN.
- `PUT /api/v1/accounts/{id}`: Modifies mutable account holder name.
- `PATCH /api/v1/accounts/{id}/balance`: Thread-safe credit/debit with audit reason.
- `DELETE /api/v1/accounts/{id}`: Deactivates account with `204 NO CONTENT`.

### Step 5: Transforming Exceptions into RFC 7807 Problem Details
Exceptions are never leaked as unformatted stack traces. They are mapped into standardized problem responses.

In [GlobalExceptionHandler.java](file:///d:/projects/safariintegration/src/main/java/com/safaricom/mpesa/exception/GlobalExceptionHandler.java#L18-L25):
```java
@ExceptionHandler(InsufficientFundsException.class)
public ProblemDetail handleInsufficientFunds(InsufficientFundsException ex) {
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
    problem.setTitle("Insufficient Funds");
    problem.setType(URI.create("https://developer.safaricom.et/errors/insufficient-funds"));
    problem.setProperty("timestamp", Instant.now());
    return problem;
}
```

---

## 3. Real-World API Integration Example
When a customer pays a merchant via QR code at a supermarket:
1. The POS scanner makes an HTTP `POST` to `/api/v1/mpesa/payments/c2b`.
2. The POS passes `Authorization: Bearer <TOKEN>` and `X-Idempotency-Key: e82e8...`.
3. If the mobile connection drops right after the server processes the debit, the POS retries with the **same** `X-Idempotency-Key`.
4. The gateway recognizes the key, reads the cached response from `idempotency_records`, and returns `200 OK` with the exact same transaction reference without charging the customer twice.
