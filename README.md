# Pharmacy POS (JavaFX)

A desktop point-of-sale application for a pharmacy, built with JavaFX and MySQL. Cashiers sell medicines and print PDF receipts; admins also manage the medicine catalog and review sales.

> University project built by a team of two (2025): **Jacob Zino** ([@jacob-zino](https://github.com/jacob-zino)) and **Magaly El Khoury** ([@magalykhoury](https://github.com/magalykhoury)).

## Features
- **Login with roles:** `ADMIN` and `CASHIER` see different screens. Passwords are stored as BCrypt hashes.
- **Point of sale:** search medicines, build a cart, and complete a sale. Each sale and its line items are saved in a single JDBC transaction (commit/rollback), then stock is reduced.
- **PDF receipts:** generated with Apache PDFBox.
- **Medicine management (admin):** add, edit and delete medicines, with price, stock, expiry date and prescription flag.
- **Sales history (admin):** view sales by date range, see order details, delete orders.

## Tech stack
Java 21 · JavaFX 21 · MySQL (JDBC, mysql-connector-j) · Apache PDFBox 3 · jBCrypt · Gradle

## Project structure
```
src/main/java/pharmacy_pos/
  Main.java               login screen and role-based navigation
  POSWindow.java          point-of-sale screen, receipts
  MedicinesWindow.java    medicine management
  TodaysSalesWindow.java  sales history
  *DAO.java               JDBC data access (users, medicines, sales)
  DBConnection.java       connection settings from environment variables
  CreateUser.java         command-line helper to add users
schema.sql                database schema
```

## Getting started
**Prerequisites:** JDK 21 and MySQL 8 (or MariaDB 10.4+). Gradle downloads JavaFX and the other libraries automatically.

1. Create the database:
   ```bash
   mysql -u root -p -e "CREATE DATABASE pharmacy_pos CHARACTER SET utf8mb4"
   mysql -u root -p pharmacy_pos < schema.sql
   ```
2. Set the connection environment variables (defaults shown):

   | Variable | Default |
   |---|---|
   | `DB_URL` | `jdbc:mysql://localhost:3306/pharmacy_pos?useSSL=false&serverTimezone=UTC` |
   | `DB_USER` | `root` |
   | `DB_PASSWORD` | *(empty)* |

3. Create a login:
   ```bash
   ./gradlew createUser --args="admin <password> ADMIN"     # Windows: gradlew.bat
   ```
4. Run the app:
   ```bash
   ./gradlew run
   ```

## Notes
- Password hashing (BCrypt), environment-based database settings and the Gradle build were added when the project was published; the original coursework version was a NetBeans project.
