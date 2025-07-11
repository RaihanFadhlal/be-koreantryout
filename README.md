# TEKOR - EPS-TOPIK Test Platform

TEKOR is a robust backend service for an online platform designed to help users prepare for the EPS-TOPIK (Employment Permit System - Test of Proficiency in Korean) exam. It provides a comprehensive set of features including user authentication, test package management, simulated test-taking, transaction handling via Midtrans, and AI-powered evaluation of test results using Google Gemini.

## Table of Contents

- [Why TEKOR?](#why-tekor)
- [General Architecture](#general-architecture)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Running the server](#running-the-server)
- [Configuration](#configuration)
- [API Endpoints](#api-endpoints)
  - [Authentication](#authentication)
  - [Users](#users)
  - [Test Packages](#test-packages)
  - [Bundles](#bundles)
  - [Test Attempts](#test-attempts)
  - [Transactions](#transactions)
  - [AI Evaluation](#ai-evaluation)
  - [Vocabularies](#vocabularies)
- [Contributing](#contributing)

## Why TEKOR?

TEKOR offers a complete solution for EPS-TOPIK preparation, featuring:

- **Realistic Test Simulation**: Mimics the actual EPS-TOPIK exam environment.
- **Secure Payments**: Integrates with Midtrans for reliable and secure transactions.
- **AI-Powered Feedback**: Provides insightful evaluation of user performance using Google Gemini.
- **Comprehensive User Management**: Handles user authentication, profiles, and progress tracking.
- **Flexible Content Management**: Allows administrators to easily manage test packages and bundles.

## General Architecture

The TEKOR backend follows a layered architecture, ensuring a clean separation of concerns and maintainability. The main components are:

- **Controllers**: Handle incoming HTTP requests and delegate to services.
- **Services**: Contain the core business logic of the application.
- **Repositories**: Interact with the PostgreSQL database using Spring Data JPA.
- **Entities**: Represent the data model of the application.
- **DTOs (Data Transfer Objects)**: Used to transfer data between layers and in API responses.

The application is built with Java and the Spring Boot framework, utilizing Maven for dependency management. It is designed to be containerized with Docker for easy deployment and scalability.

## Getting Started

### Prerequisites

- Java 17
- Maven
- PostgreSQL
- Docker (optional)

### Running the server

1.  **Clone the repository:**
    ```sh
    git clone https://github.com/your-username/be-tekor.git
    cd be-tekor
    ```
2.  **Configure the environment:**
    - Create a `.env` file from the `.env.example` and update the values for your environment.
3.  **Run the application:**
    - **Using Maven:**
      ```sh
      mvn spring-boot:run
      ```
    - **Using Docker:**
      ```sh
      docker-compose up
      ```
The server will be running on `http://localhost:8081`.

## Configuration

The application is configured through the `.env` file. Key configuration options include:

- **Database Connection**: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`
- **JWT Authentication**: `JWT_SECRET`, `JWT_EXPIRATION_ACCESS_TOKEN`, `JWT_EXPIRATION_REFRESH_TOKEN`
- **Email Service**: `EMAIL_SECRET_KEY`
- **Cloudinary**: `CLOUDINARY_API_KEY`, `CLOUDINARY_API_SECRET`, `CLOUDINARY_CLOUD_NAME`
- **Midtrans**: `MIDTRANS_SERVER_KEY`, `MIDTRANS_CLIENT_KEY`
- **Google AI**: `GOOGLE_API_KEY`, `GOOGLE_CLOUD_PROJECT`, `GOOGLE_CLOUD_LOCATION`

## API Endpoints

### Authentication

| Method | Endpoint                      | Description                                                              |
| :----- | :---------------------------- | :----------------------------------------------------------------------- |
| `POST` | `/api/v1/auth/register`       | Register a new user.                                                     |
| `POST` | `/api/v1/auth/login`          | Authenticate a user and receive a JWT.                                   |
| `GET`  | `/api/v1/auth/verify`         | Verify a user's email address.                                          |
| `POST` | `/api/v1/auth/forgot-password`| Request a password reset link.                                           |
| `GET`  | `/api/v1/auth/reset-password` | Show the password reset page.                                            |
| `POST` | `/api/v1/auth/reset-password` | Reset a user's password.                                                |
| `POST` | `/api/v1/auth/verify-mobile`  | Verify a user's email from a mobile device.                             |

### Users

| Method   | Endpoint                      | Description                                                              |
| :------- | :---------------------------- | :----------------------------------------------------------------------- |
| `GET`    | `/api/v1/users`               | Get the profile of the currently authenticated user.                     |
| `PATCH`  | `/api/v1/users`               | Update the profile of the currently authenticated user.                  |
| `POST`   | `/api/v1/users/avatar`        | Update the profile picture of the currently authenticated user.          |
| `POST`   | `/api/v1/users/change-password`| Change the password of the currently authenticated user.                 |
| `GET`    | `/api/v1/users/all`           | Get a list of all users (Admin only).                                    |
| `GET`    | `/api/v1/users/{id}`          | Get the details of a specific user (Admin only).                         |

### Test Packages

| Method   | Endpoint                      | Description                                                              |
| :------- | :---------------------------- | :----------------------------------------------------------------------- |
| `POST`   | `/api/v1/test-packages`       | Create a new test package from an Excel file (Admin only).               |
| `PUT`    | `/api/v1/test-packages/{id}`  | Update an existing test package (Admin only).                            |
| `GET`    | `/api/v1/test-packages/{id}`  | Get a specific test package by its ID.                                   |
| `DELETE` | `/api/v1/test-packages/{id}`  | Delete a test package (Admin only).                                      |
| `GET`    | `/api/v1/test-packages`       | Get a list of all available test packages and bundles.                   |
| `GET`    | `/api/v1/test-packages/all`   | Get a list of all test packages.                                         |

### Bundles

| Method   | Endpoint                | Description                                                              |
| :------- | :---------------------- | :----------------------------------------------------------------------- |
| `POST`   | `/api/v1/bundles`       | Create a new bundle of test packages (Admin only).                       |
| `GET`    | `/api/v1/bundles`       | Get a list of all available bundles.                                     |
| `GET`    | `/api/v1/bundles/{id}`  | Get a specific bundle by its ID.                                         |
| `PATCH`  | `/api/v1/bundles/{id}`  | Update an existing bundle (Admin only).                                  |
| `DELETE` | `/api/v1/bundles/{id}`  | Delete a bundle (Admin only).                                            |

### Test Attempts

| Method | Endpoint                               | Description                                                              |
| :----- | :------------------------------------- | :----------------------------------------------------------------------- |
| `POST` | `/api/v1/test-attempts/start/{packageId}`| Start a new test attempt for a specific package.                         |
| `POST` | `/api/v1/test-attempts/{attemptId}/answer`| Save the user's answer for a question in a test attempt.                |
| `POST` | `/api/v1/test-attempts/{attemptId}/submit`| Submit a test attempt for grading.                                       |
| `GET`  | `/api/v1/test-attempts/my-tests`       | Get a list of the current user's test attempts.                         |
| `GET`  | `/api/v1/test-attempts/my-tests/completed`| Get a list of the current user's completed test attempts.               |
| `GET`  | `/api/v1/test-attempts/{attemptId}/details`| Get the details of a specific test attempt.                              |
| `GET`  | `/api/v1/test-attempts/{attemptId}/review`| Get a review of a specific test attempt.                                 |

### Transactions

| Method | Endpoint                         | Description                                                              |
| :----- | :------------------------------- | :----------------------------------------------------------------------- |
| `POST` | `/api/v1/transactions/create`    | Create a new transaction to purchase a test package or bundle.           |
| `POST` | `/api/v1/transactions/midtrans/webhook`| Handle webhook notifications from Midtrans.                              |
| `GET`  | `/api/v1/transactions/status/{orderId}`| Check the status of a transaction.                                       |
| `GET`  | `/api/v1/transactions/history`   | Get the transaction history of the currently authenticated user.         |
| `GET`  | `/api/v1/transactions/{userId}`  | Get the transaction history of a specific user.                          |

### AI Evaluation

| Method | Endpoint                               | Description                                                              |
| :----- | :------------------------------------- | :----------------------------------------------------------------------- |
| `GET`  | `/api/v1/ai-evaluations/{testAttemptId}`| Get the AI-powered evaluation for a specific test attempt.               |

### Vocabularies

| Method   | Endpoint                         | Description                                                              |
| :------- | :------------------------------- | :----------------------------------------------------------------------- |
| `POST`   | `/api/v1/vocabularies/upload`    | Upload a list of vocabularies from an Excel file.                        |
| `GET`    | `/api/v1/vocabularies/categories`| Get a list of all available vocabulary categories.                       |
| `GET`    | `/api/v1/vocabularies`           | Get a paginated list of vocabularies for a specific category.            |
| `DELETE` | `/api/v1/vocabularies/{id}`      | Delete a specific vocabulary entry.                                      |

## Contributing

Contributions are welcome. Please fork the repository and submit a pull request with your changes.
