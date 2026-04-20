# Study Platform — Backend

AI-Powered Digital Study Group Ecosystem built with Spring Boot.

## Prerequisites

- Java 21+
- Docker & Docker Compose
- Maven 3.9+

## Quick Start

### 1. Start PostgreSQL

```bash
docker-compose up -d
```

This starts a PostgreSQL 16 instance on port 5432 with database `studyplatform`.

### 2. Run the application

```bash
./mvnw spring-boot:run
```

The server starts on `http://localhost:8080`.

### 3. Test the auth endpoints

**Register:**
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "email": "john@example.com",
    "password": "password123",
    "accountType": "STUDENT",
    "educationLevel": "UNIVERSITY",
    "registrationMode": "SOLO",
    "preferenceDomains": ["COMPUTER_SCIENCE", "MATHEMATICS"],
    "objectives": "Learn Java and algorithms"
  }'
```

**Login:**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "password": "password123"
  }'
```

**Get current user (authenticated):**
```bash
curl http://localhost:8080/api/users/me \
  -H "Authorization: Bearer <your-access-token>"
```

**Refresh token:**
```bash
curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "<your-refresh-token>"
  }'
```

## Project Structure

```
com.studyplatform
├── config/          # Security, CORS, WebSocket configs
├── controller/      # REST endpoints
├── dto/auth/        # Request/response objects
├── entity/          # JPA entities
├── enums/           # Shared enumerations
├── exception/       # Error handling
├── repository/      # Spring Data JPA interfaces
├── security/        # JWT, auth filter, UserDetails
└── service/         # Business logic
```

## Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `JWT_SECRET` | dev default | JWT signing key (change in prod) |
| `AWS_S3_BUCKET` | studyplatform-documents | S3 bucket name |
| `AWS_REGION` | eu-west-1 | AWS region |
| `CLAUDE_API_KEY` | — | Anthropic API key |
