# Payment Orchestration Service - API Behavior Document

## 1. API Overview

The Payment Orchestration Service provides RESTful endpoints for initiating and tracking payment transactions. It incorporates idempotency to handle duplicate requests gracefully and orchestrates interactions with various payment providers.

**Base URL**: `http://localhost:8080/payments` (assuming default Spring Boot configuration)

## 2. API Endpoints

### 2.1. Create Payment

Initiates a new payment transaction or retrieves the status of an existing idempotent request.

*   **Endpoint**: `POST /payments`
*   **Purpose**: To request a new payment. The service will process this request, handle idempotency, and initiate an asynchronous payment orchestration flow.
*   **Request**:
    *   **Method**: `POST`
    *   **Headers**:
        *   `Content-Type`: `application/json`
    *   **Body**: `CreatePaymentRequest` (JSON object)
        ```json
        {
          "merchantReference": "ORDER-12345",
          "amount": 10000,
          "currency": "USD",
          "paymentMethod": "CARD"
        }
        ```
        *   `merchantReference` (String): A unique reference from the merchant for this payment.
        *   `amount` (Long): The payment amount in the smallest currency unit (e.g., cents for USD).
        *   `currency` (String): The currency code (e.g., "USD", "INR").
        *   `paymentMethod` (String): The payment method (e.g., "CARD", "BANK_TRANSFER").

*   **Response**: `CreatePaymentResponse` (JSON object)
    *   **`201 Created`**:
        *   **Scenario**: A new payment request has been successfully received and initiated. The payment processing with external providers is now underway asynchronously.
        *   **Body**:
            ```json
            {
              "httpStatus": 201,
              "paymentId": "a1b2c3d4-e5f6-7890-1234-567890abcdef",
              "status": "PROCESSING",
              "provider": null
            }
            ```
            *   `httpStatus` (Integer): The HTTP status code of the response.
            *   `paymentId` (String): The unique identifier assigned to the newly created payment.
            *   `status` (String): The current status of the payment (e.g., "PROCESSING").
            *   `provider` (String): The name of the provider that processed the payment (will be `null` initially for new payments).
    *   **`200 OK`**:
        *   **Scenario**: An idempotent request was received, and a previous request with the same fingerprint has already completed (either `SUCCESS` or `FAILED`). The cached final result is returned.
        *   **Body**:
            ```json
            {
              "httpStatus": 200,
              "paymentId": "a1b2c3d4-e5f6-7890-1234-567890abcdef",
              "status": "SUCCESS", // or "FAILED"
              "provider": "Stripe" // or "PayPal", etc.
            }
            ```
            *   `httpStatus` (Integer): The HTTP status code of the response.
            *   `paymentId` (String): The unique identifier of the original payment.
            *   `status` (String): The final status of the payment ("SUCCESS" or "FAILED").
            *   `provider` (String): The name of the provider that successfully processed the payment, or `null` if all attempts failed.
    *   **`202 Accepted`**:
        *   **Scenario**: An idempotent request was received, and a previous request with the same fingerprint is still in `PROCESSING` state.
        *   **Body**:
            ```json
            {
              "httpStatus": 202,
              "paymentId": null, // paymentId might be null if still in early processing
              "status": "PROCESSING",
              "provider": null
            }
            ```
            *   `httpStatus` (Integer): The HTTP status code of the response.
            *   `paymentId` (String): The unique identifier of the original payment (may be `null` if not yet fully assigned or retrieved).
            *   `status` (String): The current status of the payment ("PROCESSING").
            *   `provider` (String): Will be `null` as the payment is still processing.

*   **Behavior**:
    *   **Idempotency**: The service automatically generates an idempotency key based on a SHA-256 hash of the `CreatePaymentRequest` body (merchantReference, amount, currency, paymentMethod). This key is used to detect and handle duplicate requests within a 5-minute window.
    *   **Asynchronous Orchestration**: After the initial payment record is saved, the actual interaction with external payment providers (including retries and failovers) is performed asynchronously in a separate thread. The `201 Created` response indicates that the request has been accepted for processing, not that it has completed.

### 2.2. Fetch Payment

Retrieves the detailed status and history of a specific payment transaction.

*   **Endpoint**: `GET /payments/{id}`
*   **Purpose**: To query the current status and all associated provider attempts for a given payment ID.
*   **Request**:
    *   **Method**: `GET`
    *   **URL**: `/payments/{id}`
        *   `{id}` (String, Path Variable): The unique `paymentId` of the transaction to fetch.

*   **Response**: `FetchPaymentResponse` (JSON object)
    *   **`200 OK`**:
        *   **Scenario**: A payment with the given `paymentId` was found.
        *   **Body**:
            ```json
            {
              "payment": {
                "id": 1,
                "paymentId": "a1b2c3d4-e5f6-7890-1234-567890abcdef",
                "merchantReference": "ORDER-12345",
                "amount": 10000,
                "currency": "USD",
                "paymentMethod": "CARD",
                "status": "SUCCESS", // or "PROCESSING", "FAILED"
                "attempts": 2,
                "createdAt": "2023-10-27T10:00:00Z",
                "updatedAt": "2023-10-27T10:01:30Z"
              },
              "attempts": [
                {
                  "id": 101,
                  "paymentId": "a1b2c3d4-e5f6-7890-1234-567890abcdef",
                  "provider": "Stripe",
                  "attemptNo": 1,
                  "status": "FAILED",
                  "response": "{\"message\":\"Card declined\"}",
                  "createdAt": "2023-10-27T10:00:10Z"
                },
                {
                  "id": 102,
                  "paymentId": "a1b2c3d4-e5f6-7890-1234-567890abcdef",
                  "provider": "PayPal",
                  "attemptNo": 2,
                  "status": "SUCCESS",
                  "response": "{\"status\":\"COMPLETED\"}",
                  "createdAt": "2023-10-27T10:01:20Z"
                }
              ]
            }
            ```
            *   `payment` (Payment object): Details of the payment transaction.
            *   `attempts` (Array of ProviderAttempt objects): A list of all attempts made to process this payment, ordered by creation time.
    *   **`404 Not Found`**:
        *   **Scenario**: No payment with the given `paymentId` exists.
        *   **Body**: Empty.

*   **Behavior**:
    *   Retrieves the `Payment` entity and all associated `ProviderAttempt` records from the database.
    *   If the payment is not found, returns a `404 Not Found` response.
