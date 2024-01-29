# Bank Account Management System

This Spring application provides a RESTful API for managing bank accounts, including creating accounts, depositing money, transferring funds between accounts, and checking account balances.

## Getting Started

These instructions will help you get the project up and running on your local machine for development and testing purposes, whether you're using Linux or Windows.

### Prerequisites

- Java JDK 17 or later
- Maven 3.2+ (if not using the Maven Wrapper)

### Running the Application

1. **Clone the repository:**
    ```bash
    git clone https://github.com/Christian-G-J/bankaccountapi.git
    ```

2. **Build the project:**
    - Using Maven:
        ```bash
        mvn clean install
        ```
    - Or using the Maven Wrapper:
        - For Linux:
            ```bash
            ./mvnw clean install
            ```
        - For Windows:
            ```bash
            mvnw.cmd clean install
            ```

3. **Run the application:**
    - Using Maven:
        ```bash
        mvn spring-boot:run
        ```
    - Or using the Maven Wrapper:
        - For Linux:
            ```bash
            ./mvnw spring-boot:run
            ```
        - For Windows:
            ```bash
            mvnw.cmd spring-boot:run
            ```

The application should now be running and accessible at `http://localhost:8080`.

Additionally, you can access the database console at `http://localhost:8080/h2`.

### Database Initialization

Upon startup, the application will automatically populate the in-memory H2 database with a set of predefined bank accounts for immediate use and testing.

## API Endpoints

The following endpoints are available:

### Create Account

- **Method:** POST
- **Endpoint:** `/accounts`
- **Description:** Creates a new bank account.
- **Payload Example:**
    ```json
    {
      "accountNumber": "123456",
      "firstName": "John",
      "lastName": "Doe",
      "balance": "1000.00"
    }
    ```
- **cURL Example:**
    ```bash
    curl -X POST http://localhost:8080/accounts \
    -H 'Content-Type: application/json' \
    -d '{
        "accountNumber": "123456",
        "firstName": "John",
        "lastName": "Doe",
        "balance": "1000.00"
    }'
    ```

### Deposit Money

- **Method:** PUT
- **Endpoint:** `/accounts/{accountNumber}/deposit`
- **Description:** Deposits money into an account.
- **Parameters:** `amount`
- **cURL Example:**
    ```bash
    curl -X PUT 'http://localhost:8080/accounts/123456/deposit?amount=500'
    ```

### Transfer Money

- **Method:** PUT
- **Endpoint:** `/accounts/{sourceAccountNumber}/transfer`
- **Description:** Transfers money from one account to another.
- **Parameters:** `destinationAccountNumber`, `amount`
- **cURL Example:**
    ```bash
    curl -X PUT 'http://localhost:8080/accounts/123456/transfer?destinationAccountNumber=654321&amount=200'
    ```

### Get Account Balance

- **Method:** GET
- **Endpoint:** `/accounts/{accountNumber}/balance`
- **Description:** Retrieves the balance of a specific account.
- **cURL Example:**
    ```bash
    curl -X GET http://localhost:8080/accounts/123456/balance
    ```

## Running Tests

To run the automated tests for this system:
   - Using Maven:
     ```bash
     mvn test
     ```
   - Or using the Maven Wrapper:
     - For Linux:
       ```bash
       ./mvnw test
       ```
     - For Windows:
       ```bash
       mvnw.cmd test
       ```
