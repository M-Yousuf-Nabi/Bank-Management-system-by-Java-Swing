# 🏦 M-Yousuf-Nabi Banking System

A full-featured desktop banking application built with **Java Swing** and **MySQL**, supporting both a graphical user interface (GUI) and a terminal-based menu interface. Designed for managing customer and employee banking operations with real-time database connectivity.

---

## 📋 Table of Contents

- [Features](#features)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Database Schema](#database-schema)
- [Getting Started](#getting-started)
- [Running the Application](#running-the-application)
- [Usage Guide](#usage-guide)
- [Class Overview](#class-overview)
- [Security Notes](#security-notes)
- [Contributing](#contributing)

---

## ✨ Features

### 👤 Customer
- Register and log in securely
- Open **Savings** or **Fixed** bank accounts
- **Deposit** funds into any account
- **Withdraw** funds (PIN-protected)
- **Transfer** funds between accounts (PIN-protected)
- View full **transaction history** per account
- **Update security PIN** for any account
- **Close** an account (only when balance is zero)
- Edit personal profile (name, email, password, address, phone)
- Delete own profile (cascades to accounts and transactions)

### 🧑‍💼 Employee
- Log in to the employee dashboard
- View all registered **customers** and **employees** in tables
- Register new customers or employees via dialog
- Edit own employee profile (name, email, password, job title, salary)
- Delete own employee profile

### 🖥️ Dual Interface
- **Swing GUI** (`SwingApp.java`) — modern Nimbus-themed desktop UI
- **Terminal CLI** (`Main.java`) — Scanner-based menu for console usage

---

## 🛠️ Tech Stack

| Layer        | Technology              |
|--------------|-------------------------|
| Language     | Java 11+                |
| UI Framework | Java Swing (Nimbus LAF) |
| Database     | MySQL                   |
| JDBC Driver  | MySQL Connector/J       |
| DB Hosting   | Railway (remote MySQL)  |

---

## 📁 Project Structure

```
banking-system/
│
├── Main.java                    # CLI entry point
├── SwingApp.java                # GUI entry point
│
├── model/
│   ├── Person.java              # Abstract base class
│   ├── Customer.java            # Customer entity
│   ├── Employee.java            # Employee entity
│   ├── Account.java             # Bank account entity
│   └── Transaction.java         # Transaction entity
│
├── service/
│   ├── AuthService.java         # Login & registration logic (UI-facing)
│   ├── UserService.java         # User CRUD operations
│   ├── CustomerService.java     # Customer CRUD operations
│   ├── EmployeeService.java     # Employee CRUD operations
│   ├── AccountService.java      # Account operations (CLI)
│   ├── AccountOperationsService.java  # Account operations (GUI wrapper)
│   └── TransactionService.java  # Transaction recording & history
│
├── ui/
│   ├── LoginFrame.java          # Login window
│   ├── RegisterDialog.java      # Registration dialog
│   ├── CustomerDashboardFrame.java  # Customer main window
│   └── EmployeeDashboardFrame.java  # Employee main window
│
├── menu/
│   └── Menu.java                # CLI menu dispatcher
│
└── config/
    └── DatabaseConfig.java      # JDBC connection configuration
```

> **Note:** All `.java` files are in the default package. The folder structure above reflects logical grouping.

---

## 🗄️ Database Schema

The application expects the following tables in your MySQL database:

```sql
CREATE TABLE users (
    user_id   INT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100)        NOT NULL,
    email     VARCHAR(100) UNIQUE NOT NULL,
    password  VARCHAR(255)        NOT NULL,
    role      ENUM('CUSTOMER', 'EMPLOYEE') NOT NULL
);

CREATE TABLE customers (
    customer_id  INT AUTO_INCREMENT PRIMARY KEY,
    user_id      INT         NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    address      VARCHAR(255),
    phone_number VARCHAR(20)
);

CREATE TABLE employees (
    employee_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id     INT            NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    job_title   VARCHAR(100),
    salary      DECIMAL(10, 2)
);

CREATE TABLE accounts (
    account_number BIGINT PRIMARY KEY,
    customer_id    INT            NOT NULL REFERENCES customers(customer_id) ON DELETE CASCADE,
    account_type   ENUM('SAVINGS', 'FIXED') NOT NULL,
    balance        DECIMAL(15, 2) DEFAULT 0.00,
    security_pin   VARCHAR(4)     NOT NULL
);

CREATE TABLE transactions (
    transaction_id   INT AUTO_INCREMENT PRIMARY KEY,
    account_number   BIGINT        NOT NULL REFERENCES accounts(account_number) ON DELETE CASCADE,
    transaction_type ENUM('DEPOSIT', 'WITHDRAWAL', 'TRANSFER_IN', 'TRANSFER_OUT') NOT NULL,
    amount           DECIMAL(15, 2) NOT NULL,
    description      VARCHAR(255),
    transaction_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

---

## 🚀 Getting Started

### Prerequisites

- **Java JDK 11** or higher
- **MySQL Connector/J** JAR (e.g. `mysql-connector-j-8.x.x.jar`)
- A MySQL database (local or hosted — the project ships pre-configured for a Railway instance)

### 1. Clone the Repository

```bash
git clone https://github.com/your-username/banking-system.git
cd banking-system
```

### 2. Configure the Database

Open `DatabaseConfig.java` and update the connection details to point to your own MySQL instance:

```java
private static final String URL      = "jdbc:mysql://your-host:port/your-database";
private static final String USER     = "your-username";
private static final String PASSWORD = "your-password";
```

### 3. Add the MySQL JDBC Driver

Download [MySQL Connector/J](https://dev.mysql.com/downloads/connector/j/) and place the `.jar` file in a `lib/` folder at the project root.

### 4. Compile

```bash
javac -cp ".;lib/mysql-connector-j-*.jar" *.java
# On macOS/Linux use : instead of ;
javac -cp ".:lib/mysql-connector-j-*.jar" *.java
```

---

## ▶️ Running the Application

### GUI Mode (Swing)

```bash
java -cp ".;lib/mysql-connector-j-*.jar" SwingApp
```

### CLI Mode (Terminal)

```bash
java -cp ".;lib/mysql-connector-j-*.jar" Main
```

> On macOS/Linux, replace `;` with `:` in the classpath.

---

## 📖 Usage Guide

### First Time Setup
1. Launch the app and click **Register**
2. Choose role: **Customer** or **Employee**
3. Fill in your details and submit
4. You will be automatically logged in after registration

### Customer Dashboard
- **Open Account** — Create a new Savings or Fixed account; your PIN is auto-generated and displayed once
- **Deposit** — Select an account and enter the amount
- **Withdraw** — Requires your 4-digit security PIN
- **Transfer** — Requires source account, destination account number, amount, and PIN
- **Transactions** — Select an account and view full history in a scrollable table
- **Update PIN** — Select an account and set a new 4-digit PIN
- **Close Account** — Only allowed when balance is $0.00; type `DELETE` to confirm
- **Profile Panel** — Edit your details on the right and click **Save**

### Employee Dashboard
- View all customers and employees in sortable tables
- Click **Register New** to add a new user
- Edit your own profile details in the right panel and click **Save**

---

## 🏗️ Class Overview

| Class | Responsibility |
|---|---|
| `Person` | Abstract base: `userId`, `fullName`, `email`, `password` |
| `Customer` | Extends `Person`; adds `customerId`, `address`, `phoneNumber` |
| `Employee` | Extends `Person`; adds `employeeId`, `jobTitle`, `salary` |
| `Account` | Holds account data: number, type, balance, PIN |
| `Transaction` | Holds transaction record: type, amount, description, timestamp |
| `DatabaseConfig` | Provides JDBC `Connection` via `getConnection()` |
| `AuthService` | Non-interactive login & registration used by the Swing UI |
| `UserService` | CRUD on the `users` table; email checks, updates, deletes |
| `CustomerService` | CRUD on the `customers` table |
| `EmployeeService` | CRUD on the `employees` table |
| `AccountService` | Full account operations for the CLI (Scanner-based) |
| `AccountOperationsService` | UI-friendly wrapper around `AccountService` & `TransactionService` |
| `TransactionService` | Records transactions and retrieves history |
| `Menu` | CLI menu dispatcher — routes logged-in users to the right actions |
| `LoginFrame` | Swing login window with email/password validation |
| `RegisterDialog` | Modal registration form with field validation |
| `CustomerDashboardFrame` | Full customer Swing UI |
| `EmployeeDashboardFrame` | Full employee Swing UI |
| `SwingApp` | GUI entry point; applies Nimbus Look & Feel |
| `Main` | CLI entry point; Scanner-driven main loop |

---

## 🔒 Security Notes

> This project is intended for **educational purposes**. The following practices are **not suitable for production**:

- Passwords are stored in **plain text** — use bcrypt or Argon2 hashing in production
- Security PINs are stored in **plain text** — should be hashed
- Database credentials are **hardcoded** in `DatabaseConfig.java` — use environment variables or a secrets manager
- No HTTPS/TLS is enforced on the JDBC connection

---

## 🤝 Contributing

Contributions are welcome! To contribute:

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/your-feature`
3. Commit your changes: `git commit -m "Add your feature"`
4. Push to the branch: `git push origin feature/your-feature`
5. Open a Pull Request

---

## 👨‍💻 Authors

**M-Yousuf-Nabi**

---

## 📄 License

This project is open source and available under the [MIT License](LICENSE).
