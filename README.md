# Spring-Auth — Token-Based API Gateway

Microservices auth system with Spring Boot, Nginx `auth_request`, PostgreSQL, RabbitMQ and Mailpit.
Nginx validates every request via a subrequest to the auth service before forwarding to backends.

## Architecture
```
Client ──► Nginx (:80)
              ├─ /register, /verify, /auth/*    →  auth-service  ←→  PostgreSQL
              ├─ /admin/*                       →  auth-service (public)
              ├─ /a/*  ──[auth_request]──►  service-a
              └─ /b/*  ──[auth_request]──►  service-b
                                            RabbitMQ  ──►  notification-service  ──►  Mailpit
```
For protected routes (`/a/`, `/b/`), Nginx subrequests `GET /auth/validate`.
Returns **200** → forwarded, otherwise → **403**.

## Tech Stack
| Component            | Technology                 | Port                     |
|----------------------|----------------------------|--------------------------|
| API Gateway          | Nginx (alpine)             | 80                       |
| Auth Service         | Spring Boot 3.4 / Java 17  | 8080 (internal)          |
| Notification Service | Spring Boot 3.4 / Java 17  | 8081 (internal)          |
| Database             | PostgreSQL 16              | 5432                     |
| Message Broker       | RabbitMQ 3                 | 5672 / 15672 (UI)        |
| Mail Server          | Mailpit                    | 1025 (SMTP) / 8025 (UI) |
| DB Admin             | pgAdmin 4                  | 5050                     |

## Quick Start
```bash
sudo docker compose up --build -d
```

## API Documentation (Swagger / OpenAPI)
- Swagger UI (direct auth-service): `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON (direct auth-service): `http://localhost:8080/v3/api-docs`
- (Optional via Nginx): `http://localhost/swagger-ui/index.html` and `http://localhost/v3/api-docs`

To test protected endpoints in Swagger UI:
1. Login using `POST /auth/email/login` and copy the token.
2. Click **Authorize**.
3. Enter the raw token value in the `bearerAuth` field.
4. Call secured endpoints (`/auth/me`, `/admin/*`, etc.).

## API Endpoints
### Public (no token required)
| Method | Path                          | Description                          |
|--------|-------------------------------|--------------------------------------|
| POST   | `/register`                   | Register — body: `{email, password}` |
| GET    | `/verify?tokenId=...&t=...`   | Verify email via link from inbox     |
| POST   | `/auth/email/login`           | Login — body: `{email, password}`    |
| GET    | `/admin/identities`           | List all users                       |
| GET    | `/admin/identities/connected` | List connected users                 |
| DELETE | `/admin/identities/{email}`   | Delete a user                        |
| GET    | `/admin/credentials`          | List all credentials                 |

### Protected (`Authorization: <token>` header required)
| Method | Path                    | Description                            |
|--------|-------------------------|----------------------------------------|
| GET    | `/a/`, `/b/`            | Proxied to service A / B               |
| GET    | `/auth/me`              | Current user info                      |
| POST   | `/auth/logout`          | Invalidate token                       |
| POST   | `/auth/change-password` | Body: `{oldPassword, newPassword}`     |

## Usage Example
```bash
# 1. Register
curl -s http://localhost/register -X POST -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"secret123"}'
# 2. Verify email — open Mailpit at http://localhost:8025, click the link
# 3. Login
curl -s http://localhost/auth/email/login -X POST -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"secret123"}'
# → {"token":"<uuid>"}
# 4. Access protected services
curl http://localhost/a/ -H "Authorization: <uuid>"   # → Hello A
curl http://localhost/b/ -H "Authorization: <uuid>"   # → Hello B
```

## Local Development
```bash
sudo docker compose up postgres rabbitmq mailpit -d
cd authentification && mvn spring-boot:run
```

## Configuration
Each service uses a single `application.yml`. The verification email base URL is configurable
via `app.verification.base-url` in the notification service.

## Project Structure
```
├── nginx/nginx.conf             # Gateway with auth_request
├── authentification/            # Auth service (Spring Boot)
│   └── src/main/java/demo/
│       ├── controller/          # REST endpoints
│       ├── service/             # Business logic
│       ├── model/               # JPA entities
│       ├── repository/          # Spring Data repos
│       ├── exception/           # Custom exceptions + global handler
│       └── config/              # Security & RabbitMQ
├── notification/                # Notification service (Spring Boot)
├── service-a/ & service-b/     # Pseudo-services ("Hello A/B")
└── docker-compose.yml
```

## Admin UIs
| UI       | URL                    | Credentials                 |
|----------|------------------------|-----------------------------|
| RabbitMQ | http://localhost:15672 | `guest` / `guest`           |
| Mailpit  | http://localhost:8025  | —                           |
| pgAdmin  | http://localhost:5050  | `admin@admin.com` / `admin` |

## Authors
- BIERHOFF Theolien
- CAMARA Ibrahima
- DIALLO Mamadou Ougailou
- JULES Stevenson

