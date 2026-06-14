# Secure Employee Management System (SEMS)

Midterm project for Information Security. The goal is not to build a full HR product, but to demonstrate practical security controls around employee data.

## Stack

- Java 21
- Spring Boot 3.5
- Spring Security
- JWT
- Spring Data JPA
- MySQL 8
- Spring Mail / Email OTP
- Maven Wrapper

## Environment Setup

Create a local `.env` from `.env.example` or set variables directly in PowerShell. Do not commit real secrets.

```powershell
$env:APP_NAME="SEMS"
$env:DB_USERNAME="root"
$env:DB_PASSWORD="root"
$env:JWT_SECRET="replace-with-a-long-random-secret-at-least-32-characters"
$env:MAIL_HOST="smtp.gmail.com"
$env:MAIL_PORT="465"
$env:MAIL_USERNAME="your_email@gmail.com"
$env:MAIL_PASSWORD="your_app_password"
$env:MAIL_FROM="SEMS <your_email@gmail.com>"
$env:MAIL_SMTP_AUTH="true"
$env:MAIL_SMTP_STARTTLS_ENABLE="false"
$env:MAIL_SMTP_SSL_ENABLE="true"
$env:LOGIN_RATE_LIMIT_MAX_REQUESTS="5"
$env:LOGIN_RATE_LIMIT_WINDOW_SECONDS="60"
```

## MySQL

Default connection:

```text
jdbc:mysql://localhost:3306/sems_db
```

Local development values:

```text
DB_USERNAME=root
DB_PASSWORD=root
```

The application can create `sems_db` automatically when MySQL is running.

## Run

Use JDK 21.

```powershell
$env:JAVA_HOME="C:\Program Files\Eclipse Adoptium\jdk-21.0.8.9-hotspot"
$env:Path="$env:JAVA_HOME\bin;$env:Path"
$env:DB_USERNAME="root"
$env:DB_PASSWORD="root"

.\mvnw.cmd clean compile spring-boot:run
```

Alternative jar flow:

```powershell
.\mvnw.cmd clean package
java -jar target\sems-0.0.1-SNAPSHOT.jar
```

## Test

```powershell
$env:JAVA_HOME="C:\Program Files\Eclipse Adoptium\jdk-21.0.8.9-hotspot"
$env:Path="$env:JAVA_HOME\bin;$env:Path"
$env:DB_USERNAME="root"
$env:DB_PASSWORD="root"

.\mvnw.cmd -q clean test
```

## Seed Accounts

When `SEED_DATA_ENABLED=true`, the application creates demo accounts:

| Username | Email | Password | Role |
| --- | --- | --- | --- |
| admin | admin@sems.local | Password@123 | ADMIN |
| manager | manager@sems.local | Password@123 | MANAGER |
| employee | employee@sems.local | Password@123 | EMPLOYEE |

Permission model:

- ADMIN has all permissions.
- MANAGER can create/view/edit employees.
- EMPLOYEE can view only their own employee profile.

## Main Login Flow

Login is a 2-step flow:

1. `POST /api/auth/login`
   - Send username and password.
   - If valid, the server sends an OTP to email.
   - No JWT is returned yet.
2. `POST /api/auth/verify-otp`
   - Send username and OTP.
   - If valid, the server returns a JWT.

Use JWT in protected requests:

```text
Authorization: Bearer <accessToken>
```

## Core APIs

| Group | Endpoint |
| --- | --- |
| Auth | `POST /api/auth/register`, `POST /api/auth/login`, `POST /api/auth/verify-otp` |
| Users | `GET/POST /api/users`, `GET/PUT/DELETE /api/users/{id}` |
| User security | `PATCH /api/users/{id}/lock`, `PATCH /api/users/{id}/unlock`, `PATCH /api/users/{id}/roles` |
| Employees | `GET/POST /api/employees`, `GET/PUT/DELETE /api/employees/{id}` |
| Audit | `GET /api/audit-logs?page=0&size=20` |
| Login history | `GET /api/login-history?page=0&size=20` |

## Security Features

- BCrypt password hashing.
- Password policy: minimum length, uppercase, lowercase, number, special character.
- Email OTP second factor.
- JWT protected API.
- RBAC and permission-based access control.
- Account lockout after repeated failed logins.
- Login rate limiting with HTTP 429.
- Login history for SUCCESS and FAILED events.
- Audit log for security-sensitive actions.
- Standard JSON error response.

## Internal Demo Assets

Internal files are stored under `Do not push github/` and are excluded from Git:

```text
Do not push github/plan/DEMO_SCRIPT.md
Do not push github/plan/SEMS.postman_collection.json
Do not push github/plan/PROJECT_PLAN.md
```
