# Appointment Reservation System  
A secure, database-backed appointment reservation platform built with Java, JDBC, and Azure SQL Server.
The system supports user authentication, role-based workflows, scheduling logic, and vaccine inventory management.
It is fully compatible with cloud deployment and is designed to run seamlessly with a remote **Azure SQL Database**.

---

## 🚀 Overview

This project implements a complete **appointment scheduling backend**, capable of managing users, caregivers, vaccine inventory, and reservations.  
It demonstrates secure credential storage, transactional DB operations, domain-driven application logic, and a well-structured Java architecture.

Key capabilities include:

- Secure login with salted + hashed passwords  
- Role-based workflows (patients & caregivers)  
- Automatic appointment matching based on availability  
- Vaccine inventory management  
- JDBC data access layer with prepared statements  
- SQL Server relational schema with strong integrity constraints  
- Clean, modular Java code design

---

## ☁️ Azure Deployment (SQL Database)

This project is designed to work directly with an **Azure SQL Server instance**.

### 🔹 1. Create an Azure SQL Database
- Create a **SQL Database** and **SQL Server** in the Azure portal  
- Enable "Allow Azure services and resources to access this server"
- Add your local IP to the database firewall if testing locally

### 🔹 2. Configure Database Credentials
You will receive connection parameters such as:

---

## 🧱 Architecture

The system is composed of three main layers:

### **1. Database Layer (SQL Server on Azure)**
- Tables for **patients**, **caregivers**, **vaccines**, and **appointments**
- Password security using cryptographic salts and PBKDF2 hashing
- Data consistency maintained through foreign keys and transactional updates

### **2. Java Backend (JDBC + Modular OOP Design)**
- `ConnectionManager` abstracts database connectivity  
- Use of **PreparedStatement** for safety against SQL injection  
- Domain classes: `Patient`, `Caregiver`, `Vaccine`, `Appointment`  
- Atomic operations ensuring correct scheduling and inventory updates  
- Clear separation between model, persistence, and application logic

### **3. Command-Line Interface (Application Layer)**
- Interactive CLI for all operations
- Input validation and helpful error recovery
- Stateful session management for logged-in users
- Structured command system supporting both patient and caregiver operations

---

## 🧠 Core Features

### 🔐 **Secure Authentication**
- Password hashing with PBKDF2 + random salt  
- No plaintext password storage  
- Consistent, secure login workflow  

### 👥 **Role-Based Functionality**

#### **Patients**
- Create account  
- Log in / log out  
- Search caregiver availability  
- Schedule vaccination appointments  
- View upcoming appointments  

#### **Caregivers**
- Upload availability  
- Add vaccine doses  
- Manage inventory  
- View scheduled patient appointments  

### 📅 **Appointment Scheduling Engine**
- Automatically matches patients to available caregivers  
- Prevents overbooking  
- Reduces vaccine doses atomically  
- Generates unique, traceable appointment records  

---



