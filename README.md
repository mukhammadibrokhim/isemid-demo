# 🦠 ISEMID - Information System for Epidemiological Monitoring of Infectious Diseases

ISEMID is a web-based system developed to register, track, and monitor infectious diseases. It is designed for use by medical institutions and epidemiological departments to manage cases, generate official forms, and submit statistical reports.

---

## 📦 Tech Stack

- **Java**: 21  
- **Spring Boot**: 3.3.5
- **Maven**: for project build and dependency management  
- **PostgreSQL**: as the primary database  
- **OAuth2 SSO**: secure authentication  
- **Feature-based Packaging**: for better modularization and scalability

---

## 📁 Project Structure

This project uses a **feature-based packaging** strategy:

```
src/main/java/uz/uzinfocom/isemid/
├── config/             # Global configurations
├── commons/            # Shared DTOs, Enums, Exceptions, Utilities
├── features/
│   ├── patient/        # Patient-related logic
│   ├── card/           # Card161, Card174, etc.
│   ├── form058/        # Form058 handling
│   ├── journal/        # Journal060 and related models
│   ├── organization/   # Organization and MedCore sync
│   ├── report/         # Reporting and analytics
│   └── ...             # Additional feature modules
```

---

## 🚀 How to Run the Project

### ✅ Prerequisites

Ensure the following are installed on your system:

- Java 21
- Maven 3.6+
- PostgreSQL
- Git

---

### 📂 Clone the Repository

```bash
git clone https://git.ssv.uz/ses/emis/backend.git
cd backend
```

---

### ⚙️ Configure Application Properties

Modify the following file to set database and authentication configurations:

📄 `src/main/resources/application.yml`

Example:

```yaml
server:
  port: 8081

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/isemid
    username: postgres
    password: your_password
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
```

---

### 🛠️ Build the Project

```bash
mvn clean install
```

---

### ▶️ Run the Application

#### Option 1: Using Maven

```bash
mvn spring-boot:run
```

#### Option 2: Using the Jar File

```bash
java -jar target/isemid-*.jar
```

---

### 🌐 Access the Application

After starting the app, open:

```
http://localhost:8081
```

---

## 🌐 API Endpoints

All endpoints are prefixed with `/v1`.

Examples:

- `GET /v1/patient`
- `POST /v1/card`
- `PUT /v1/form058/{id}`

API paths are organized using constants in `ApiPaths.java`.

---

## 🔐 Security

- OAuth2 SSO-based token authentication
- Role-based access control
- Internal fallback login with `nnuzb` and password

---

## 🔧 Developer Notes

- Uses DTOs for API request and response structures
- Mapping between DTO and entities is done using MapStruct
- All database operations use Spring Data JPA
- Repositories support pagination and filtering
- `@Transactional` used in service layer where needed

---

## 🌍 External Integrations

ISEMID is integrated with:

- **MedCore.uz** (FHIR-based core system)
- **SSV SSO** system
- **GCP APIs** (Citizen, Child)
- **Vaccination registry**

---

## 👥 Contributors

- Developed and maintained by Uzinfocom SES team  
- Contact: `m_tursinboyev@uzinfocom.uz`

---

## 📄 License

© 2025 Uzinfocom. All rights reserved.