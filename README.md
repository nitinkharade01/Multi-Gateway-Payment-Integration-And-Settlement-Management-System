# Multi-Gateway Payment Integration and Settlement Management System

Java 21 Spring Boot microservices project for fintech payment gateway integration, settlement, reconciliation, refunds, fraud monitoring, and merchant notification workflows.

## Overview

The platform models a payment orchestration backend with merchant onboarding, API-key validation, payment order creation, simulated gateway routing, webhook ingestion, refund processing, settlement generation, reconciliation reports, fraud scoring, and notification dispatch.

It is built as a Maven multi-module backend with a React frontend. The project supports local development, Docker Compose, GitHub push, and Render Java runtime deployment.

## Features

- Merchant registration, KYC/status management, webhook URL updates, and API key rotation.
- BCrypt hashing for user passwords and merchant API secrets.
- JWT authentication with environment-driven production secret and expiration.
- Idempotent payment order creation using merchant plus idempotency key.
- Gateway routing simulator for UPI, QR, card, wallet, and netbanking.
- HMAC SHA-256 webhook validation with timestamp replay protection.
- Full and partial refund validation with refundable balance checks.
- Settlement fee, GST, and net settlement calculation with `BigDecimal`.
- Gateway CSV reconciliation and mismatch reporting.
- Fraud risk scoring and alert review.
- Optional Kafka and Redis for local/Docker; disabled by default in Render profile.

## Architecture

```text
React frontend -> API Gateway -> Auth / Merchant / Payment / Gateway Routing
                              -> Webhook / Refund / Settlement / Reconciliation
                              -> Fraud Monitoring / Notification

Discovery Server: Eureka registry
Database: PostgreSQL
Optional local infra: Kafka and Redis
```

More detail is in [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md).

## Microservices

| Module | Local Port | Responsibility |
| --- | --- | --- |
| `platform-common` | n/a | Shared events, money helpers, correlation ID filter |
| `discovery-server` | `8761` | Eureka service registry |
| `api-gateway` | `8080` | Public gateway routes, CORS, correlation ID propagation |
| `auth-service` | `8081` | Registration, login, JWT, refresh tokens |
| `merchant-service` | `8082` | Merchant lifecycle, KYC, API keys, webhook config |
| `payment-service` | `8083` | Orders, transactions, idempotency, payment status |
| `gateway-routing-service` | `8084` | Gateway selection and health simulation |
| `webhook-service` | `8085` | Gateway webhook validation and processing |
| `refund-service` | `8086` | Full and partial refunds |
| `settlement-service` | `8087` | Settlement batches and fee calculations |
| `reconciliation-service` | `8088` | CSV upload, matching, mismatch reporting |
| `fraud-monitoring-service` | `8089` | Fraud scoring and alerts |
| `notification-service` | `8090` | Event-driven notification logs and callbacks |
| `frontend-react` | `5173` dev, `3000` Docker | React UI |

## Tech Stack

- Java 21 LTS
- Spring Boot 3.3.x
- Spring Cloud Gateway
- Eureka Discovery Server
- Spring Security and JWT
- Spring Data JPA and Hibernate
- PostgreSQL
- Kafka optional
- Redis optional
- Maven multi-module build
- React and Vite frontend
- Docker optional
- Render deployment

## Java 21 Setup

Confirm Java and Maven both use Java 21:

```powershell
java -version
mvn -version
```

The root [pom.xml](pom.xml) sets:

```xml
<java.version>21</java.version>
<maven.compiler.release>21</maven.compiler.release>
```

The Maven compiler plugin uses `<release>21</release>`.

## Local Setup

Detailed steps are in [docs/LOCAL_SETUP.md](docs/LOCAL_SETUP.md).

Build all backend modules:

```powershell
mvn clean install -DskipTests
```

Recommended local environment:

```powershell
$env:SPRING_PROFILES_ACTIVE='local'
$env:DATABASE_URL='jdbc:postgresql://localhost:5432/payment_platform_db'
$env:DB_USERNAME='postgres'
$env:DB_PASSWORD='root123'
$env:KAFKA_ENABLED='true'
$env:REDIS_ENABLED='true'
```

Run services locally:

```powershell
mvn -pl discovery-server spring-boot:run
mvn -pl api-gateway spring-boot:run
mvn -pl auth-service spring-boot:run
mvn -pl merchant-service spring-boot:run
mvn -pl payment-service spring-boot:run
mvn -pl gateway-routing-service spring-boot:run
mvn -pl webhook-service spring-boot:run
mvn -pl refund-service spring-boot:run
mvn -pl settlement-service spring-boot:run
mvn -pl reconciliation-service spring-boot:run
mvn -pl fraud-monitoring-service spring-boot:run
mvn -pl notification-service spring-boot:run
```

Docker Compose:

```powershell
mvn clean package -DskipTests
$env:POSTGRES_HOST_PORT='5433'
docker compose up --build -d
docker compose ps
```

## Frontend

```powershell
cd frontend-react
npm install
$env:VITE_API_BASE_URL='http://localhost:8080'
npm run dev
```

For Render frontend deployment:

```text
VITE_API_BASE_URL=https://<api-gateway-service>.onrender.com
```

Recommended Render frontend setup:

```text
Type: Static Site
Root Directory: frontend-react
Build Command: npm install && npm run build
Publish Directory: dist
```

Docker frontend setup is also supported:

```text
Type: Web Service
Root Directory: frontend-react
Docker Build Context Directory: .
Dockerfile Path: ./Dockerfile
Health Check Path: /
```

## Render Deployment

Full Render instructions are in [docs/RENDER_DEPLOYMENT.md](docs/RENDER_DEPLOYMENT.md).

Render PostgreSQL JDBC URL:

```text
jdbc:postgresql://dpg-d88jb6h9rddc738g4nv0-a:5432/payment_platform_db
```

Do not commit the password. Set it only in Render:

```text
DB_PASSWORD=<DB_PASSWORD_FROM_RENDER>
```

Use this common environment:

```text
SPRING_PROFILES_ACTIVE=render
DATABASE_URL=jdbc:postgresql://dpg-d88jb6h9rddc738g4nv0-a:5432/payment_platform_db
DB_USERNAME=payment_user
DB_PASSWORD=<DB_PASSWORD_FROM_RENDER>
EUREKA_SERVER_URL=https://<discovery-server-service-name>.onrender.com/eureka/
KAFKA_ENABLED=false
REDIS_ENABLED=false
CORS_ALLOWED_ORIGINS=https://<frontend-service>.onrender.com,http://localhost:3000,http://localhost:5173
JWT_SECRET=<LONG_SECURE_SECRET>
JWT_EXPIRATION=86400000
WEBHOOK_SECRET=<SECURE_WEBHOOK_SECRET>
API_SECRET_PEPPER=<SECURE_API_SECRET_PEPPER>
```

Render health check path for every service:

```text
/actuator/health
```

### Single Render Web Service

The root `Dockerfile` supports running all backend modules in one Render Docker Web Service. In the Render form, use:

```text
Language: Docker
Root Directory: leave blank
Docker Build Context Directory: .
Dockerfile Path: ./Dockerfile
Docker Command: leave blank
Health Check Path: /actuator/health
```

Set `EUREKA_SERVER_URL=http://localhost:8761/eureka/` for the single-container deployment. The API Gateway listens on Render's `$PORT`; the other services run on internal ports `8761`, `8081` through `8090`.

The container starts API Gateway early so Render can detect the public port. Optional startup tuning:

```text
API_GATEWAY_STARTUP_DELAY_SECONDS=2
DISCOVERY_STARTUP_DELAY_SECONDS=20
SERVICE_STARTUP_DELAY_SECONDS=2
```

All backend services in one container need more memory than a normal single service. Render Free may not be enough for the full platform; use a larger instance or set `ENABLED_SERVICES` to a smaller comma-separated list.

## Render Build And Start Commands

Discovery Server:

```text
Build: mvn clean package -DskipTests -pl discovery-server -am
Start: java -jar discovery-server/target/discovery-server-1.0.0-SNAPSHOT.jar --spring.profiles.active=render
```

API Gateway:

```text
Build: mvn clean package -DskipTests -pl api-gateway -am
Start: java -jar api-gateway/target/api-gateway-1.0.0-SNAPSHOT.jar --spring.profiles.active=render
```

Business services follow the same pattern:

```text
mvn clean package -DskipTests -pl <module-name> -am
java -jar <module-name>/target/<module-name>-1.0.0-SNAPSHOT.jar --spring.profiles.active=render
```

Examples:

```text
mvn clean package -DskipTests -pl payment-service -am
java -jar payment-service/target/payment-service-1.0.0-SNAPSHOT.jar --spring.profiles.active=render
```

## PostgreSQL Setup

Render DB variables:

```text
DATABASE_URL=jdbc:postgresql://dpg-d88jb6h9rddc738g4nv0-a:5432/payment_platform_db
DB_USERNAME=payment_user
DB_PASSWORD=<DB_PASSWORD_FROM_RENDER>
```

Local DB variables:

```text
DATABASE_URL=jdbc:postgresql://localhost:5432/payment_platform_db
DB_USERNAME=postgres
DB_PASSWORD=root123
```

## API Endpoints

Auth:

- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/auth/refresh`
- `GET /api/auth/profile`

Merchant:

- `POST /api/merchants/register`
- `GET /api/merchants/{merchantId}`
- `PUT /api/merchants/{merchantId}/webhook`
- `POST /api/merchants/{merchantId}/api-key`
- `PUT /api/merchants/{merchantId}/status`
- `POST /api/merchants/credentials/validate`

Payment and gateway:

- `POST /api/payments/orders`
- `GET /api/payments/orders/{orderId}`
- `POST /api/payments/{orderId}/pay`
- `GET /api/payments/status/{transactionId}`
- `GET /api/payments/merchant/{merchantId}`
- `POST /api/gateway/route`
- `GET /api/gateway/health`

Webhook, refund, settlement, reconciliation:

- `POST /api/webhooks/razorpay`
- `POST /api/webhooks/cashfree`
- `POST /api/webhooks/payu`
- `POST /api/refunds`
- `GET /api/refunds/{refundId}`
- `POST /api/settlements/generate`
- `POST /api/reconciliation/upload`
- `POST /api/reconciliation/run`

Fraud and notifications:

- `POST /api/fraud/assess`
- `GET /api/fraud/alerts/{merchantId}`
- `GET /api/notifications/merchant/{merchantId}`

Sample JSON and CSV files are in [samples](samples). A starter Postman collection is in [postman_collection.json](postman_collection.json).

## Troubleshooting

- Hibernate cannot determine dialect: confirm `DATABASE_URL` uses `jdbc:postgresql://...` and `DB_USERNAME`/`DB_PASSWORD` are set.
- Render port issue: use `server.port=${PORT:8080}` through the `render` profile.
- Kafka errors on free tier: set `KAFKA_ENABLED=false`.
- Redis errors on free tier: set `REDIS_ENABLED=false`.
- Gateway cannot discover services: update `EUREKA_SERVER_URL` after deploying `discovery-server`.
- Browser CORS failure: add the frontend URL to `CORS_ALLOWED_ORIGINS` on `api-gateway`.
- Do not commit `.env`; use [.env.example](.env.example) as the template.

## GitHub Push Commands

Initial push:

```bash
git init
git add .
git commit -m "Initial commit: Multi-gateway payment integration platform"
git branch -M main
git remote add origin https://github.com/nitinkharade01/Multi-Gateway-Payment-Integration-And-Settlement-Management-System.git
git push -u origin main
```

Feature branch push:

```bash
git checkout -b CFBP-render-deployment
git add .
git commit -m "Prepare project for Render deployment"
git push -u origin CFBP-render-deployment
```

If remote already exists:

```bash
git remote set-url origin https://github.com/nitinkharade01/Multi-Gateway-Payment-Integration-And-Settlement-Management-System.git
```

## Interview Explanation

This system separates the main money-moving responsibilities into focused services. The payment service owns idempotent order and transaction creation. The routing service owns gateway choice and fallback. The webhook service owns untrusted gateway callbacks, HMAC verification, replay protection, and deduplication. Refund, settlement, reconciliation, fraud, and notification services each handle their own post-payment workflow.

For deployment, the project uses Java 21, environment-driven secrets, PostgreSQL through JDBC URLs, Render-compatible dynamic ports, and optional Kafka/Redis so the demo can run on Render free tier without external brokers or caches.
