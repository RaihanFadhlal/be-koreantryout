# Transaction Feature Flow

This document outlines the step-by-step flow of the transaction feature, from user initiation to successful purchase, detailing the role of each file in the process.

## 1. User Initiates Purchase & Frontend Request

The user clicks "buy" on the frontend, which triggers a request to the backend.

-   **File:** `TransactionController.java`
-   **Purpose:** This is the entry point for all transaction-related HTTP requests.
    -   The `createTransaction` method (`POST /api/transactions/create`) receives the purchase request from the frontend.
    -   It is secured with `@PreAuthorize` to ensure only authenticated users can create a transaction.
    -   It accepts a `TransactionRequest` DTO, which contains the ID of the item to be purchased.
    -   It calls the `TransactionService` to process the business logic.
    -   Finally, it wraps the result in a `CommonResponse` and sends it back to the client.

## 2. Core Business Logic

The service layer handles the main logic of creating the transaction and interacting with other services.

-   **File:** `TransactionService.java` (Interface)
-   **Purpose:** Defines the contract for the transaction feature. It decouples the controller from the implementation details.
    -   `create(TransactionRequest request)`: Method signature for creating a new transaction.
    -   `handleMidtransNotification(Map<String, Object> payload)`: Method signature for processing webhooks from Midtrans.

-   **File:** `TransactionServiceImpl.java` (Implementation)
-   **Purpose:** Contains the concrete implementation of the business logic.
    -   It retrieves the currently logged-in `User` from the security context.
    -   It fetches the `TestPackage` or `Bundle` details from the corresponding repository to get the price and validate its existence.
    -   It creates a new `Transaction` entity, populates it with user data, item details, and amount.
    -   It calls the `MidtransService` to create the transaction with the payment gateway. This service returns the necessary details from Midtrans, including the unique `order_id` and payment token/URL.
    -   It sets the `midtransOrderId` received from Midtrans on the `Transaction` entity.
    -   Finally, it saves the complete `Transaction` object (including the `midtransOrderId`) to the database with a `PENDING` status via `TransactionRepository`.

## 3. Midtrans Payment Gateway Integration

This layer is responsible for all communication with the Midtrans API.

-   **File:** `MidtransService.java` (Interface)
-   **Purpose:** Defines a contract specifically for Midtrans-related operations, promoting separation of concerns.
    -   `createPaymentUrl(Transaction transaction)`: Method signature for creating a payment URL.

-   **File:** `MidtransServiceImpl.java` (Implementation)
-   **Purpose:** Handles the technical details of communicating with Midtrans.
    -   It uses `RestTemplate` to make an HTTP POST request to the Midtrans Snap API.
    -   It constructs the request body with transaction details (`order_id`, `gross_amount`, etc.).
    -   It sets the required authentication headers, including the Base64-encoded server key.
    -   It parses the response from Midtrans to extract the `redirect_url`.

## 4. Handling Payment Notification (Webhook)

After the user completes the payment, Midtrans sends a notification to a dedicated endpoint in our backend.

-   **File:** `TransactionController.java`
-   **Purpose:**
    -   The `handleMidtransNotification` method (`POST /api/transactions/midtrans/webhook`) acts as the webhook listener.
    -   This endpoint is public to allow Midtrans to send notifications.
    -   It receives the notification payload and passes it directly to the `TransactionService`.

-   **File:** `TransactionServiceImpl.java`
-   **Purpose:**
    -   The `handleMidtransNotification` method processes the payload.
    -   It finds the corresponding `Transaction` in the database using the `order_id` from the payload.
    -   It checks the `transaction_status` from Midtrans.
    -   If the status is `settlement` or `capture`, it updates the transaction status to `SUCCESS`. In a complete implementation, this is where it would call `TestAttemptService` to grant the user access to the purchased content.
    -   If the status is `cancel`, `deny`, or `expire`, it updates the transaction status to `FAILED`.
    -   It saves the updated transaction status back to the database.
