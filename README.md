# MyBankUML - Banking System Backend

MyBankUML is a secure, modular banking simulation backend built with **Java**. It implements a **3-Tier Layered Architecture** (Presentation, Logic, Data) and uses a local **JSON-based database** for persistence.

The system features **Role-Based Access Control (RBAC)**, enforcing strict permissions for Customers, Tellers, and Administrators.

---

## Tech Stack

*   **Language:** Java 8+
*   **Build Tool:** Maven
*   **Web Framework:** Javalin (Lightweight REST API)
*   **Data Persistence:** Jackson (JSON File Storage)
*   **Security:** BCrypt (Password Hashing)
*   **Testing:** JUnit 5

---

## Project Structure

MyBankUML/
├── data/                   # Local JSON Database (users, accounts, txns)
├── src/main/java/          # Source Code
│   ├── application/        # Business Logic (Managers)
│   ├── data/               # Data Access (JsonFileService)
│   ├── model/              # Domain Entities (POJOs)
│   ├── presentation/       # API Controllers & Server
│   └── util/               # Security Utilities
└── pom.xml                 # Maven Dependencies

Setup & Installation

1. Prerequisites

Java Development Kit (JDK) 8 or higher.

Maven installed.

2. Install Dependencies

Navigate to the project root and run:

code
Bash
download
content_copy
expand_less
mvn clean install

3. Database Initialization

Ensure the data/ folder exists in the project root. The application comes with pre-seeded data files (users.json, accounts.json, etc.).

Running the Application

You can run the application directly via Maven or your IDE.

Via Command Line:

code
Bash
download
content_copy
expand_less
mvn exec:java -Dexec.mainClass="Main"

Via IDE (IntelliJ/Eclipse):

Open the project as a Maven project.

Navigate to src/main/java/Main.java.

Right-click and select Run 'Main'.

The server will start at: http://localhost:8080

Default Credentials

The data/users.json file is pre-populated with the following users (all passwords are pass123):

Role	    Username	Password	User ID
Admin	    admin	    pass123	    U001
Teller	    teller	    pass123	    U002
Customer    customer	pass123	    U003

API Documentation
Authentication

Login

POST /api/login

Body:

code
JSON
download
content_copy
expand_less
{"username": "admin", "password": "..."}

Response: Returns a Session Token and Role.

Logout

POST /api/logout

Header: Authorization: <token>

Account Operations (Protected)

Transaction

POST /api/transaction

Header: Authorization: <token>

Body (Examples):

code
JSON
download
content_copy
expand_less
// Deposit
{"type": "DEPOSIT", "accountNumber": "A001", "amount": 500.00}

// Withdraw
{"type": "WITHDRAWAL", "accountNumber": "A001", "amount": 50.00}

// Transfer
{"type": "TRANSFER", "accountNumber": "A001", "targetAccount": "A002", "amount": 100.00}
Search (Teller/Admin Only)

GET /api/search?q=<query>

Header: Authorization: <token>

Param: q (Matches User ID, Name, or Username)

Admin Management (Admin Only)

Create User

POST /api/admin/create-user

Header: Authorization: <token>

Body:

code
JSON
download
content_copy
expand_less
{"username": "newuser", "password": "pw", "role": "TELLER", "name": "New Name"}

Update User (Status/Role/2FA)

PATCH /api/admin/users/{id}

Header: Authorization: <token>

Body:

code
JSON
download
content_copy
expand_less
{"status": "INACTIVE", "role": "TELLER", "twoFactorEnabled": true}

Testing

To run the Unit Tests (covering Login Lockout, Balance Validation, and RBAC):

code
Bash
download
content_copy
expand_less
mvn test

Security Features

BCrypt Hashing: Passwords are never stored in plain text.

Account Lockout: Accounts are locked after 5 failed login attempts.

RBAC: RoleManager validates every request to ensure Customers cannot access Admin features.

Real-time Validation: Withdrawals are checked against current balance before execution.

code
Code
download
content_copy
