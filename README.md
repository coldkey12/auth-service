# Auth Service

A microservice for authentication built with Spring Boot 4.0, providing JWT-based authentication, user management, and comprehensive audit logging.

## Features

- **JWT Authentication** — Secure token-based authentication with access and refresh tokens
- **User Management** — Registration, login, and role-based access control
- **Refresh Token Flow** — Seamless token renewal without re-authentication
- **Audit Logging** — Full audit trail using Hibernate Envers with history tables

- **API Documentation** — Interactive Swagger UI for API exploration

## Tech Stack

| Component | Technology |
|-----------|------------|
| Framework | Spring Boot 4.0 |
| Language | Java 21 |
| Database | PostgreSQL |
| Security | Spring Security + JWT (jjwt) |
| Auditing | Hibernate Envers |
| Docs | SpringDoc OpenAPI (Swagger) |
| Build | Maven |

## Prerequisites

- Java 21+
- PostgreSQL 14+
- Maven 3.9+

## Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/coldkey12/auth-service.git
cd auth-service
```

### 2. Configure the database

Create a PostgreSQL database:

```sql
CREATE DATABASE auth_db;
```

### 3. Set environment variables

```bash
export JWT_SECRET=your-secure-secret-key-min-256-bits
```

Or update `application.yml` directly.

### 4. Run the application

```bash
mvn spring-boot:run
```

The service will start on `http://localhost:8080`.

## Configuration

Key configuration in `application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/auth_db
    username: postgres
    password: your-password

jwt:
  secret: ${JWT_SECRET}
  expiration: 3600000        # Access token: 1 hour
  refresh-expiration: 86400000  # Refresh token: 24 hours
```

## API Endpoints

### Authentication

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Register a new user |
| POST | `/api/auth/login` | Authenticate and receive tokens |
| POST | `/api/auth/refresh` | Refresh access token |
| POST | `/api/auth/logout` | Invalidate tokens |

### Documentation

| Endpoint | Description |
|----------|-------------|
| `/swagger-ui.html` | Swagger UI |
| `/v3/api-docs` | OpenAPI specification |

## Data Models

### User

| Field | Type | Description |
|-------|------|-------------|
| id | UUID | Primary key |
| email | String | Unique email (used as username) |
| fullName | String | User's full name |
| password | String | BCrypt hashed password |
| role | Enum | `CLIENT`, `ADMIN`, etc. |
| enabled | Boolean | Account status |
| createdAt | LocalDateTime | Creation timestamp |
| updatedAt | LocalDateTime | Last update timestamp |

### RefreshToken

| Field | Type | Description |
|-------|------|-------------|
| id | UUID | Primary key |
| token | String | Unique refresh token |
| expiryDate | Instant | Token expiration |
| user | User | Associated user |

## Audit Logging

All entity changes are tracked using Hibernate Envers. Audited entities have corresponding `_history` tables that store:

- Revision number
- Revision type (INSERT, UPDATE, DELETE)
- Historical field values

Additionally, an `AuditLog` table captures:

- User actions (CREATE, UPDATE, DELETE, READ)
- Entity type and ID
- IP address and user agent
- Service name for distributed tracing
- Timestamps

## Security

- **Stateless Sessions** — No server-side session storage
- **BCrypt Password Hashing** — Secure password storage
- **JWT Validation** — Token signature and expiration verification
- **CORS Configuration** — Configurable cross-origin requests
- **Method-Level Security** — `@PreAuthorize` annotations supported

### Public Endpoints

The following paths are accessible without authentication:

- `/api/auth/**` — Authentication endpoints
- `/api/debug/**` — Debug endpoints (disable in production)
- `/swagger-ui/**` — API documentation
- `/v3/api-docs/**` — OpenAPI spec

## Project Structure

```
src/main/java/kz/don/auth/
├── domain/
│   ├── entity/          # JPA entities (User, RefreshToken, AuditLog)
│   └── enums/           # Enumerations (RoleEnum)
├── infrastructure/
│   └── security/
│       ├── config/      # SecurityConfig, CORS config
│       └── jwt/         # JwtAuthFilter, JWT utilities
├── application/         # Service layer
└── interfaces/          # Controllers, DTOs
```

## Running with Docker

```bash
# Start PostgreSQL
docker-compose up -d postgres

# Run the application
mvn spring-boot:run
```

## Testing

```bash
# Run all tests
mvn test

# Run with coverage
mvn test jacoco:report
```

## License

This project is licensed under the MIT License.

## Author

- **coldkey12** — [GitHub](https://github.com/coldkey12)
