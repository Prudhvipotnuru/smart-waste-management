# Smart Waste Management System

## 🚀 Overview

The **Smart Waste Management System** is a backend-driven application designed to address waste mismanagement in urban and rural areas.

It enables municipalities to track waste collection at the **household level**, enforce **segregation practices**, and gain **data-driven insights** for better decision-making.

---

## ❗ Problem Statement

In many Indian cities and villages, waste management suffers from:

* Lack of household-level tracking
* Poor waste segregation (wet/dry/mixed)
* No accountability for garbage collectors
* No real-time data for municipalities

This leads to inefficient waste processing and increased environmental impact.

---

## 💡 Solution

This system introduces a **QR-based tracking mechanism** where:

* Each house is assigned a unique QR code
* Garbage collectors scan the QR during collection
* Waste segregation status is recorded
* Data is stored and visualized through dashboards

This ensures **accountability, transparency, and improved waste segregation**.

---

## 🛠️ Features

### ✅ Core Features

* House registration with unique QR code generation
* Waste collection tracking per household
* Segregation status recording (SEGREGATED / NOT_SEGREGATED)
* Collector-based tracking system

### 📊 Dashboard

* Total number of houses
* Total waste collection entries
* Segregated vs non-segregated count

### 📂 Batch Processing

* CSV upload for bulk house registration
* Partial success handling
* Error CSV generation for failed records

### ⚙️ Backend Capabilities

* RESTful APIs using Spring Boot
* DTO-based architecture
* Validation and global exception handling
* Clean layered architecture (Controller → Service → Repository)

---

## 🧱 Tech Stack

* **Java**
* **Spring Boot**
* **Spring Data JPA**
* **Hibernate**
* **PostgreSQL**
* **Spring Batch** (for CSV processing)

---

## 🏗️ System Architecture

```
Controller → Service → Repository → Database
```

* **Controller**: Handles API requests
* **Service**: Business logic
* **Repository**: Database interaction
* **DTOs**: Data transfer between layers

---

## 📌 API Endpoints (Sample)

### House APIs

* `POST /houses` → Create a new house
* `GET /houses` → Get all houses

### Waste Collection APIs

* `POST /waste-collections` → Record waste collection
* `GET /waste-collections` → Get all records

### Dashboard

* `GET /dashboard` → Get summary statistics

---

## ▶️ How to Run

1. Clone the repository:

   `git clone https://github.com/Prudhvipotnuru/smart-waste-management.git`

2. Open in your IDE (IntelliJ / Eclipse)

3. Configure database in `application.properties`

4. Run the Spring Boot application

5. Test APIs using Postman

---

## 📈 Future Enhancements

* QR code scanning via mobile app
* AI-based waste image validation
* Citizen mobile app for feedback
* Reward system for proper segregation
* Real-time tracking dashboard

---

## 👨‍💻 Author

**Prudhvi Potnuru**

---

## 🌱 Vision

To build a scalable, low-cost waste management system that can be deployed in **small towns and rural areas**, improving environmental sustainability through technology.
