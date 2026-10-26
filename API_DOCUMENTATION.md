# M-PESA & Core Banking Integration Gateway — API Specification & Reference Guide

This document provides a comprehensive reference for all RESTful API endpoints exposed by the **M-PESA Fintech Bridge**. It details required headers, request payloads, response structures, HTTP status codes, validation rules, and testing examples for Postman and cURL.

---

## Table of Contents
1. [General API Standards & Headers](#general-api-standards--headers)
2. [Authentication Endpoints (`/api/v1/auth`)](#1-authentication-endpoints-apiv1auth)
   - `POST /api/v1/auth/login` — Generate Partner JWT Bearer Token
3. [Payment & Settlement Endpoints (`/api/v1/mpesa/payments`)](#2-payment--settlement-endpoints-apiv1mpesapayments)
   - `POST /api/v1/mpesa/payments/c2b` — Process Customer-to-Business (C2B) Payment
4. [Account Management Endpoints (`/api/v1/accounts`)](#3-account-management-endpoints-apiv1accounts)
   - `POST /api/v1/accounts` — Register New Financial Account
   - `GET /api/v1/accounts` — Retrieve All Accounts
   - `GET /api/v1/accounts/{id}` — Fetch Account by ID
   - `GET /api/v1/accounts/search` — Query Account by MSISDN / Account Number
   - `PUT /api/v1/accounts/{id}` — Update Account Profile Metadata
   - `PATCH /api/v1/accounts/{id}/balance` — Adjust Account Balance (Credit / Debit)
   - `DELETE /api/v1/accounts/{id}` — Delete / Deactivate Account
5. [Transaction & Audit Endpoints (`/api/v1/transactions`)](#4-transaction--audit-endpoints-apiv1transactions)
   - `GET /api/v1/transactions` — Query Complete Transaction Audit Trail
   - `GET /api/v1/transactions/{reference}` — Fetch Transaction Status by Reference
   - `POST /api/v1/transactions/{reference}/reverse` — Reverse Transaction & Compensate Funds
6. [Error Handling & RFC 7807 Problem Details](#5-error-handling--rfc-7807-problem-details)

---

## General API Standards & Headers

### Base URL
- Local Development: `http://localhost:8080`
- Kubernetes Ingress / Production: `https://api.safaricom.et/fintech`

### Standard Headers
| Header Name | Type | Description | Required On |
| :--- | :--- | :--- | :--- |
| `Content-Type` | `application/json` | Media type of the request payload | All `POST`, `PUT`, `PATCH` requests |
| `Authorization` | `Bearer <token>` | Signed HMAC-SHA256 JWT Bearer token | All protected endpoints (except `/api/v1/auth/**`) |
| `X-Idempotency-Key`| `string (UUID)` | Unique transaction identifier preventing duplicate debit operations | Financial transactions (`/c2b`) |

---

## 1. Authentication Endpoints (`/api/v1/auth`)

### 1.1 Generate Partner JWT Bearer Token
Issues a cryptographically signed HMAC-SHA token for accredited fintech partners, merchant aggregators, or internal services.

- **Method**: `POST`
- **URL**: `/api/v1/auth/login`
- **Authentication**: Public (No Bearer Token required)
- **Headers**:
  ```http
  Content-Type: application/json
  ```

#### Request Body
```json
{
  "username": "safari_partner",
  "password": "partner@123"
}
```

| Field | Type | Required | Constraints | Description |
| :--- | :--- | :--- | :--- | :--- |
| `username` | `string` | Yes | Non-blank | Accredited fintech partner username |
| `password` | `string` | Yes | Non-blank | Partner authentication password or API key |

#### Response: `200 OK`
```json
{
  "token": "eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiJzYWZhcmlfcGFydG5lciIsImlhdCI6MTc5Mjk4NTY0MSwiZXhwIjoxNzkzMDcyMDQxfQ...",
  "expiresIn": 86400000
}
```

| Field | Type | Description |
| :--- | :--- | :--- |
| `token` | `string` | URL-safe signed JWT Bearer string to pass in the `Authorization` header |
| `expiresIn` | `integer` | Token Time-To-Live in milliseconds (86,400,000 ms = 24 hours) |

#### Error Responses
- `401 Unauthorized`: Invalid credentials provided.

#### Example cURL
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"safari_partner","password":"partner@123"}'
```

---

## 2. Payment & Settlement Endpoints (`/api/v1/mpesa/payments`)

### 2.1 Process Customer-to-Business (C2B) Payment
Orchestrates end-to-end customer wallet debit and downstream core banking ledger credit with idempotency guarantees.

- **Method**: `POST`
- **URL**: `/api/v1/mpesa/payments/c2b`
- **Authentication**: `Bearer <token>` (Required)
- **Headers**:
  ```http
  Content-Type: application/json
  Authorization: Bearer <jwt_token>
  X-Idempotency-Key: 123e4567-e89b-12d3-a456-426614174000
  ```

#### Request Body
```json
{
  "senderMsisdn": "251712345678",
  "receiverBankAccount": "1000123456789",
  "amount": 500.00,
  "currency": "ETB"
}
```

| Field | Type | Required | Constraints | Description |
| :--- | :--- | :--- | :--- | :--- |
| `senderMsisdn` | `string` | Yes | Regex: `^(2517[0-9]{8}\|2519[0-9]{8})$` | Valid Ethiopian mobile number (Safaricom / Ethio Telecom) |
| `receiverBankAccount` | `string` | Yes | Non-blank | Target destination bank account number |
| `amount` | `decimal` | Yes | Min: `1.00` | Transaction monetary value |
| `currency` | `string` | Yes | Value: `"ETB"` | ISO-4217 3-letter currency code |

#### Response: `200 OK` (Processed or Duplicate Idempotent Response)
```json
{
  "transactionReference": "MPESA1792985689974506E",
  "status": "SUCCESS",
  "message": "Payment processed successfully",
  "coreBankingTransactionId": "CORE-1F831115",
  "timestamp": "2026-10-26T06:34:50.0456502"
}
```

| Field | Type | Description |
| :--- | :--- | :--- |
| `transactionReference` | `string` | Unique alphanumeric M-PESA audit identifier |
| `status` | `string` | Settlement state: `SUCCESS` or `FAILED` |
| `message` | `string` | Human-readable transaction summary or rejection note |
| `coreBankingTransactionId`| `string` | Reference ID generated by Core Banking SOAP service |
| `timestamp` | `string` | ISO-8601 server timestamp of transaction completion |

#### Error Responses
- `400 Bad Request`: Validation failure (invalid phone format, amount < 1.00 ETB, or Insufficient Funds).
- `403 Forbidden`: Missing or expired Bearer token.

#### Example cURL
```bash
curl -X POST http://localhost:8080/api/v1/mpesa/payments/c2b \
  -H "Authorization: Bearer <TOKEN>" \
  -H "X-Idempotency-Key: c2b-req-001" \
  -H "Content-Type: application/json" \
  -d '{
    "senderMsisdn": "251712345678",
    "receiverBankAccount": "1000123456789",
    "amount": 500.00,
    "currency": "ETB"
  }'
```

---

## 3. Account Management Endpoints (`/api/v1/accounts`)

### 3.1 Register New Financial Account
Creates a new customer mobile money wallet or institutional settlement account on the gateway.

- **Method**: `POST`
- **URL**: `/api/v1/accounts`
- **Authentication**: `Bearer <token>` (Required)

#### Request Body
```json
{
  "accountNumber": "251911223344",
  "accountHolderName": "Derartu Tulu",
  "initialBalance": 10000.00,
  "currency": "ETB"
}
```

| Field | Type | Required | Constraints | Description |
| :--- | :--- | :--- | :--- | :--- |
| `accountNumber` | `string` | Yes | Unique, valid MSISDN or 10-20 digit account number | Primary unique account identifier |
| `accountHolderName`| `string` | Yes | 2 to 100 characters | Full legal name of customer or merchant entity |
| `initialBalance` | `decimal` | Yes | Min: `0.00` | Starting monetary balance |
| `currency` | `string` | Yes | Value: `"ETB"` | Account operating currency |

#### Response: `201 Created`
```json
{
  "id": 3,
  "accountNumber": "251911223344",
  "accountHolderName": "Derartu Tulu",
  "balance": 10000.0000,
  "currency": "ETB",
  "version": 0
}
```

---

### 3.2 Retrieve All Accounts
Returns a list of all accounts registered on the bridge.

- **Method**: `GET`
- **URL**: `/api/v1/accounts`
- **Authentication**: `Bearer <token>` (Required)

#### Response: `200 OK`
```json
[
  {
    "id": 1,
    "accountNumber": "251712345678",
    "accountHolderName": "Abebe Kebede",
    "balance": 4500.0000,
    "currency": "ETB",
    "version": 1
  },
  {
    "id": 2,
    "accountNumber": "251988776655",
    "accountHolderName": "Almaz Ayana",
    "balance": 3500.0000,
    "currency": "ETB",
    "version": 0
  }
]
```

---

### 3.3 Fetch Account by ID
Looks up account details using internal database primary key.

- **Method**: `GET`
- **URL**: `/api/v1/accounts/{id}`
- **Authentication**: `Bearer <token>` (Required)
- **Path Parameter**: `id` (e.g., `1`)

#### Response: `200 OK`
```json
{
  "id": 1,
  "accountNumber": "251712345678",
  "accountHolderName": "Abebe Kebede",
  "balance": 4500.0000,
  "currency": "ETB",
  "version": 1
}
```

#### Error Responses
- `404 Not Found`: Account with specified ID does not exist.

---

### 3.4 Query Account by Number / MSISDN
Looks up an account using its public MSISDN or bank account string.

- **Method**: `GET`
- **URL**: `/api/v1/accounts/search?accountNumber=251712345678`
- **Authentication**: `Bearer <token>` (Required)
- **Query Parameter**: `accountNumber` (Required)

#### Response: `200 OK`
```json
{
  "id": 1,
  "accountNumber": "251712345678",
  "accountHolderName": "Abebe Kebede",
  "balance": 4500.0000,
  "currency": "ETB",
  "version": 1
}
```

---

### 3.5 Update Account Profile Metadata
Updates mutable account details (such as account holder name) without modifying balance.

- **Method**: `PUT`
- **URL**: `/api/v1/accounts/{id}`
- **Authentication**: `Bearer <token>` (Required)
- **Path Parameter**: `id` (e.g., `1`)

#### Request Body
```json
{
  "accountHolderName": "Abebe Kebede Gizaw"
}
```

#### Response: `200 OK`
```json
{
  "id": 1,
  "accountNumber": "251712345678",
  "accountHolderName": "Abebe Kebede Gizaw",
  "balance": 4500.0000,
  "currency": "ETB",
  "version": 1
}
```

---

### 3.6 Adjust Account Balance (Credit / Debit)
Atomically credits or debits an account balance with optimistic concurrency locking and regulatory audit reason tracking.

- **Method**: `PATCH`
- **URL**: `/api/v1/accounts/{id}/balance`
- **Authentication**: `Bearer <token>` (Required)
- **Path Parameter**: `id` (e.g., `1`)

#### Request Body (Credit Example)
```json
{
  "amount": 1000.00,
  "type": "CREDIT",
  "reason": "Agent Cash Deposit Settlement"
}
```

#### Request Body (Debit Example)
```json
{
  "amount": 250.00,
  "type": "DEBIT",
  "reason": "Monthly Maintenance Fee Deduction"
}
```

| Field | Type | Required | Constraints | Description |
| :--- | :--- | :--- | :--- | :--- |
| `amount` | `decimal` | Yes | Min: `0.01` | Balance adjustment amount |
| `type` | `string` | Yes | `"CREDIT"` or `"DEBIT"` | Adjustment operation type |
| `reason` | `string` | Yes | Non-blank | Regulatory audit justification |

#### Response: `200 OK`
```json
{
  "id": 1,
  "accountNumber": "251712345678",
  "accountHolderName": "Abebe Kebede",
  "balance": 5500.0000,
  "currency": "ETB",
  "version": 2
}
```

---

### 3.7 Delete Account
Deletes an account from the system by ID.

- **Method**: `DELETE`
- **URL**: `/api/v1/accounts/{id}`
- **Authentication**: `Bearer <token>` (Required)
- **Path Parameter**: `id` (e.g., `3`)

#### Response: `204 No Content`
*(Empty response body)*

---

## 4. Transaction & Audit Endpoints (`/api/v1/transactions`)

### 4.1 Query Complete Transaction Audit Trail
Returns all recorded financial transactions in the bridge ledger.

- **Method**: `GET`
- **URL**: `/api/v1/transactions`
- **Authentication**: `Bearer <token>` (Required)

#### Response: `200 OK`
```json
[
  {
    "id": 1,
    "transactionReference": "MPESA1792985689974506E",
    "senderAccount": "251712345678",
    "receiverAccount": "1000123456789",
    "amount": 500.0000,
    "currency": "ETB",
    "status": "SUCCESS",
    "failureReason": null,
    "createdAt": "2026-10-26T06:34:50"
  }
]
```

---

### 4.2 Fetch Transaction Status by Reference
Queries the real-time status of a specific financial transaction.

- **Method**: `GET`
- **URL**: `/api/v1/transactions/{reference}`
- **Authentication**: `Bearer <token>` (Required)
- **Path Parameter**: `reference` (e.g., `MPESA1792985689974506E`)

#### Response: `200 OK`
```json
{
  "id": 1,
  "transactionReference": "MPESA1792985689974506E",
  "senderAccount": "251712345678",
  "receiverAccount": "1000123456789",
  "amount": 500.0000,
  "currency": "ETB",
  "status": "SUCCESS",
  "failureReason": null,
  "createdAt": "2026-10-26T06:34:50"
}
```

#### Status Lifecycle Values
- `PENDING`: Transaction created, awaiting core banking dispatch.
- `SUCCESS`: Confirmed debit and settlement approved by core banking.
- `FAILED`: Transaction aborted (insufficient funds, timeout, downstream rejection).
- `REVERSED`: Transaction successfully rolled back and funds refunded.

---

### 4.3 Reverse Transaction & Compensate Funds
Initiates an administrative or dispute compensation flow. Credits the sender account back and transitions the transaction status to `REVERSED`.

- **Method**: `POST`
- **URL**: `/api/v1/transactions/{reference}/reverse`
- **Authentication**: `Bearer <token>` (Required)
- **Path Parameter**: `reference` (e.g., `MPESA1792985689974506E`)

#### Request Body (Optional)
```json
{
  "reason": "Customer wrongly sent funds to merchant account"
}
```

#### Response: `200 OK`
```json
{
  "id": 1,
  "transactionReference": "MPESA1792985689974506E",
  "senderAccount": "251712345678",
  "receiverAccount": "1000123456789",
  "amount": 500.0000,
  "currency": "ETB",
  "status": "REVERSED",
  "failureReason": "Reversal executed: Customer wrongly sent funds to merchant account",
  "createdAt": "2026-10-26T06:34:50"
}
```

---

## 5. Error Handling & RFC 7807 Problem Details

All error responses adhere to the standard **RFC 7807 (Problem Details for HTTP APIs)** specification:

### Example: Validation Error (`400 Bad Request`)
```json
{
  "type": "about:blank",
  "title": "Invalid Request Payload",
  "status": 400,
  "detail": "Validation failed",
  "instance": "/api/v1/accounts",
  "fieldErrors": {
    "accountNumber": "Account number must be a valid Ethiopian MSISDN (e.g., 251712345678)",
    "initialBalance": "Initial balance cannot be negative"
  },
  "timestamp": "2026-10-26T07:45:10.123456Z"
}
```

### Example: Insufficient Funds (`400 Bad Request`)
```json
{
  "type": "https://developer.safaricom.et/errors/insufficient-funds",
  "title": "Insufficient Funds",
  "status": 400,
  "detail": "Insufficient balance in account: 251712345678",
  "timestamp": "2026-10-26T07:45:15.987654Z"
}
```

### Example: Resource Not Found (`404 Not Found`)
```json
{
  "type": "https://developer.safaricom.et/errors/not-found",
  "title": "Resource Not Found",
  "status": 404,
  "detail": "Account not found with ID: 99",
  "timestamp": "2026-10-26T07:45:20.112233Z"
}
```

### Example: Unauthorized / Missing Token (`403 Forbidden`)
```json
{
  "timestamp": "2026-10-26T07:45:25.000+00:00",
  "status": 403,
  "error": "Forbidden",
  "path": "/api/v1/accounts"
}
```
