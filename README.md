Java Bank Management System

A simple console-based Bank Management System built using Java and MySQL (JDBC).
It allows users to register, log in, create accounts, perform transactions, and view transaction history through a text-based interface.

Features

User Registration and Login
Register as a new user or log in with existing credentials.

Account Management
Each user automatically gets an account with a unique account number.

Balance Inquiry
Check your current account balance at any time.

Transactions
Transfer money between accounts and record all details in the database.

Transaction History
View all previous transactions with date, amount, category, and status.

Technologies Used

Java (JDK 17 or higher)

MySQL

JDBC (Java Database Connectivity)

IntelliJ IDEA (or any Java IDE)

Database Schema
CREATE DATABASE IF NOT EXISTS bank_system;
USE bank_system;

CREATE TABLE IF NOT EXISTS users (
  user_id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(50),
  email VARCHAR(100) UNIQUE,
  password VARCHAR(50),
  creation_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS accounts (
  account_number INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT,
  balance DOUBLE,
  FOREIGN KEY(user_id) REFERENCES users(user_id)
);

CREATE TABLE IF NOT EXISTS transactions (
  transaction_id INT AUTO_INCREMENT PRIMARY KEY,
  debit_account INT,
  credit_account INT,
  amount DOUBLE,
  status VARCHAR(50),
  category VARCHAR(50) NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

How to Run the Project

Clone the repository

git clone https://github.com/your-username/Bank-Management-System.git
cd Bank-Management-System


Set up the MySQL Database

Open MySQL and execute the schema provided above.

Configure Database Credentials

Inside your Java code, update the JDBC connection details:

String url = "jdbc:mysql://localhost:3306/bank_system";
String user = "root";
String password = "your_password";


Compile and Run

javac BankSystem.java
java BankSystem
