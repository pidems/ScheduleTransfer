# **1. Transfer-Service-Assessment**

A simple Java web application simulating schedule money transfer operations between bank accounts. The application includes REST APIs for processing transactions, and retrieving transaction details. 

---

## **Features**
- REST APIs for money transfers and transaction management.
- Support for optional transaction filters (status, account number, date range).

---

# **2. Technology Stack**
- **Framework**: Spring Boot  
- **Language**: Java 11+  
- **Database**: H2  
- **Build Tool**: Maven  


---

## **3. Prerequisites**
1. **Java Development Kit (JDK)**: Version 11 or higher.  
2. **Maven**: Version 3.6 or higher.  
3. **Docker (optional)**: For containerized deployment.  
4. **Kubernetes (optional)**: For orchestrating multiple instances.  

---


``` 
```bash
mvn clean install
```
#### To run the application 
```bash
mvn spring-boot:run
```
---

# **5. API Documentation**
## **API Endpoints**

### 1. Process a Transaction
- **Endpoint**: `POST /api/v1/transactions/transfer`
- **Description**: Processes a money transfer request.
- **Request Body**:
```
    { 
        "reference":"test transfer",
        "amount":5.00,
        "currency":"USD",
        "sourceAccountNumber":"1234567890",
        "destinationAccountNumber":"2113182084"
    }
```

### 2. Retrieve Transactions
- **Endpoint**: `GET /api/v1/transactions`
- **Description**: Processes a money transfer request.
- **Query Parameters**:
    status (optional): Transaction status (e.g., SUCCESSFUL).
    sourceAccountNumber (optional): Filter by source account number.
    destinationAccountNumber (optional): Filter destination by account number.
    startDate and endDate (optional): Date range for transactions.

### 3. Daily Summary
- **Endpoint**: `GET /api/v1/transactions/summary`
- **Description**: Processes a money transfer request.
- **Query Parameters**:
    date (optional): The date for which to fetch the summary (default is today).

---
# **6. Running Tests**
#### 1. Unit Tests
Run unit tests with Maven:
```bash
mvn test
```



