# Secure Employee & Project Management System

Spring Boot 3 / Spring Security 6 REST API capstone. Java 21, Maven, MySQL, JWT access tokens and rotating refresh tokens.

## Implemented
- Registration with Bean Validation and BCrypt password hashing
- Login with 15-minute JWT access token and 7-day refresh token
- Refresh-token rotation; refresh tokens are stored as SHA-256 hashes and revoked on use/logout
- Five failed login attempts lock an account for 15 minutes
- Stateless JWT authentication filter
- ADMIN / MANAGER / EMPLOYEE role-based endpoint restrictions and method security
- Employee ownership check, project APIs, JSON 401/403 handling, CORS allow-list
- Global validation/error handling, Actuator health, JaCoCo report setup
- Optional bootstrap admin through environment variables

## Requirements
- JDK 21
- Maven 3.9+
- MySQL 8+

## Run locally (Windows CMD)
1. Create database:
   `CREATE DATABASE employee_project_db;`
2. Set environment variables in the same CMD window:
   ```bat
   set DB_URL=jdbc:mysql://localhost:3306/employee_project_db
   set DB_USERNAME=root
   set DB_PASSWORD=your_mysql_password
   set JWT_SECRET=replace-with-a-random-secret-at-least-32-characters-long
   set ADMIN_EMAIL=admin@example.com
   set ADMIN_PASSWORD=ChangeMe123!
   ```
   JWT_SECRET must contain at least 32 characters. Do not commit real secrets.
3. From project folder:
   `mvn clean spring-boot:run`
4. Check `http://localhost:8081/actuator/health`.

If port 8081 is occupied, use `set SERVER_PORT=8082` before starting and update the Postman base URL.

## Run with Docker
Copy `.env.example` to `.env`, change values, then run:
`docker compose up --build`

## API endpoints
Base URL: `http://localhost:8081`

| Method | Endpoint | Access |
|---|---|---|
| POST | `/api/v1/auth/register` | Public |
| POST | `/api/v1/auth/login` | Public |
| POST | `/api/v1/auth/refresh` | Public with refresh token |
| POST | `/api/v1/auth/logout` | Authenticated refresh-token revocation |
| POST | `/api/admin/users` | ADMIN |
| POST | `/api/admin/employees` | ADMIN |
| GET | `/api/projects` | ADMIN, MANAGER, EMPLOYEE |
| POST | `/api/projects` | ADMIN, MANAGER |
| DELETE | `/api/projects/{id}` | ADMIN |
| GET | `/api/employees/{id}` | ADMIN/MANAGER or own user ID |
| GET | `/actuator/health` | Public |

## Postman examples

Register:
```http
POST /api/v1/auth/register
Content-Type: application/json
```
```json
{"fullName":"Test Employee","email":"employee@example.com","password":"SecurePass123"}
```

Login:
```json
{"email":"employee@example.com","password":"SecurePass123"}
```
Copy `accessToken` and use `Authorization: Bearer <accessToken>` for secured endpoints. Keep refresh token private.

Create project (ADMIN/MANAGER):
```json
{"name":"HR Modernization","description":"Employee portal API","status":"ACTIVE","managerId":1}
```

Create employee profile (ADMIN):
```json
{"userId":1,"employeeCode":"EMP-1001","department":"Engineering","designation":"Java Developer"}
```

## Account lockout
Five incorrect password attempts lock the account for 15 minutes. A successful login resets the failure count.

## Tests / coverage
Run:
`mvn clean verify`
JaCoCo HTML report: `target/site/jacoco/index.html`.

## Security notes and limitations
- Email verification/reset flow and login rate limiting are not included in this baseline; add these before claiming full production readiness.
- Logout revokes the supplied refresh token. Already-issued access JWTs remain valid until their 15-minute expiry unless a server-side access-token denylist is added.
- Configure HTTPS, secret management, DB least privilege, migration tooling (Flyway/Liquibase), audit logs, and robust rate limiting before production.
- Review and run all tests in your own environment before submission.
