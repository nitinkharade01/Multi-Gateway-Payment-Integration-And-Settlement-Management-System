# Render Deployment

This project is ready for Render Java runtime deployment. Do not put passwords or secrets in Git. Set them in each Render service environment.

## PostgreSQL

Render internal database URL:

```text
postgresql://payment_user:<DB_PASSWORD>@dpg-d88jb6h9rddc738g4nv0-a/payment_platform_db
```

Spring Boot needs the JDBC form:

```text
jdbc:postgresql://dpg-d88jb6h9rddc738g4nv0-a:5432/payment_platform_db
```

## Common Environment Variables

| Variable | Value |
| --- | --- |
| `SPRING_PROFILES_ACTIVE` | `render` |
| `EUREKA_SERVER_URL` | `https://<discovery-server-service-name>.onrender.com/eureka/` |
| `KAFKA_ENABLED` | `false` |
| `REDIS_ENABLED` | `false` |
| `CORS_ALLOWED_ORIGINS` | `https://<frontend-service>.onrender.com,http://localhost:3000,http://localhost:5173` |

## DB Service Environment Variables

Set these for every DB-backed service.

| Variable | Value |
| --- | --- |
| `DATABASE_URL` | `jdbc:postgresql://dpg-d88jb6h9rddc738g4nv0-a:5432/payment_platform_db` |
| `DB_USERNAME` | `payment_user` |
| `DB_PASSWORD` | `<DB_PASSWORD_FROM_RENDER>` |

## Auth And Secret Variables

| Variable | Value |
| --- | --- |
| `JWT_SECRET` | `<LONG_SECURE_SECRET>` |
| `JWT_EXPIRATION` | `86400000` |
| `JWT_REFRESH_TTL_DAYS` | `7` |
| `ADMIN_DEFAULT_EMAIL` | `admin@payment.com` |
| `ADMIN_DEFAULT_PASSWORD` | `<SECURE_PASSWORD>` |
| `WEBHOOK_SECRET` | `<SECURE_WEBHOOK_SECRET>` |
| `API_SECRET_PEPPER` | `<SECURE_API_SECRET_PEPPER>` |

Use `RAZORPAY_WEBHOOK_SECRET`, `CASHFREE_WEBHOOK_SECRET`, and `PAYU_WEBHOOK_SECRET` if each gateway should have a different secret.

## Service URL Variables

Set these where the service calls another service directly:

| Service | Variables |
| --- | --- |
| `payment-service` | `MERCHANT_SERVICE_URL=https://<merchant-service>.onrender.com`, `ROUTING_SERVICE_URL=https://<gateway-routing-service>.onrender.com` |
| `webhook-service` | `PAYMENT_SERVICE_URL=https://<payment-service>.onrender.com` |
| `refund-service` | `PAYMENT_SERVICE_URL=https://<payment-service>.onrender.com` |
| `settlement-service` | `PAYMENT_SERVICE_URL=https://<payment-service>.onrender.com` |
| `reconciliation-service` | `PAYMENT_SERVICE_URL=https://<payment-service>.onrender.com` |

## Deployment Order

1. Create Render PostgreSQL database.
2. Deploy `discovery-server`.
3. Copy the discovery-server Render URL.
4. Set `EUREKA_SERVER_URL` in all services.
5. Deploy `api-gateway`.
6. Deploy `auth-service`.
7. Deploy `merchant-service`.
8. Deploy `payment-service`.
9. Deploy `gateway-routing-service`.
10. Deploy `webhook-service`.
11. Deploy `refund-service`.
12. Deploy `settlement-service`.
13. Deploy `reconciliation-service`.
14. Deploy `fraud-monitoring-service`.
15. Deploy `notification-service`.

For the first free-tier deployment, deploy only:

- `discovery-server`
- `api-gateway`
- `auth-service`
- `merchant-service`
- `payment-service`
- `gateway-routing-service`

Keep `KAFKA_ENABLED=false` and `REDIS_ENABLED=false`.

## Single Render Web Service Docker Deployment

If you want all backend modules in one Render Web Service, use the root `Dockerfile`. It builds every backend JAR and starts all services inside one container. The API Gateway is the only public HTTP entrypoint and listens on Render's `$PORT`.

Use these Render fields:

| Field | Value |
| --- | --- |
| Language | `Docker` |
| Branch | `main` |
| Root Directory | leave blank |
| Docker Build Context Directory | `.` |
| Dockerfile Path | `./Dockerfile` |
| Docker Command | leave blank |
| Health Check Path | `/actuator/health` |

Use these extra environment variables for the all-in-one container:

| Variable | Value |
| --- | --- |
| `EUREKA_SERVER_URL` | `http://localhost:8761/eureka/` |
| `MERCHANT_SERVICE_URL` | `http://localhost:8082` |
| `ROUTING_SERVICE_URL` | `http://localhost:8084` |
| `PAYMENT_SERVICE_URL` | `http://localhost:8083` |
| `ENABLED_SERVICES` | `all` |
| `API_GATEWAY_STARTUP_DELAY_SECONDS` | `2` |
| `DISCOVERY_STARTUP_DELAY_SECONDS` | `20` |
| `SERVICE_STARTUP_DELAY_SECONDS` | `2` |

All DB, JWT, webhook, Kafka, Redis, and CORS environment variables from the sections above still apply.

Running every Spring Boot service in one container is convenient for a demo, but it is memory-heavy. Render Free is likely to run out of memory with all services enabled. Use at least a 2 GB instance for the full backend, or set `ENABLED_SERVICES` to a smaller comma-separated list such as:

```text
auth-service,merchant-service,gateway-routing-service,payment-service
```

## Build And Start Commands

| Service | Build Command | Start Command |
| --- | --- | --- |
| Discovery Server | `mvn clean package -DskipTests -pl discovery-server -am` | `java -jar discovery-server/target/discovery-server-1.0.0-SNAPSHOT.jar --spring.profiles.active=render` |
| API Gateway | `mvn clean package -DskipTests -pl api-gateway -am` | `java -jar api-gateway/target/api-gateway-1.0.0-SNAPSHOT.jar --spring.profiles.active=render` |
| Auth Service | `mvn clean package -DskipTests -pl auth-service -am` | `java -jar auth-service/target/auth-service-1.0.0-SNAPSHOT.jar --spring.profiles.active=render` |
| Merchant Service | `mvn clean package -DskipTests -pl merchant-service -am` | `java -jar merchant-service/target/merchant-service-1.0.0-SNAPSHOT.jar --spring.profiles.active=render` |
| Payment Service | `mvn clean package -DskipTests -pl payment-service -am` | `java -jar payment-service/target/payment-service-1.0.0-SNAPSHOT.jar --spring.profiles.active=render` |
| Gateway Routing Service | `mvn clean package -DskipTests -pl gateway-routing-service -am` | `java -jar gateway-routing-service/target/gateway-routing-service-1.0.0-SNAPSHOT.jar --spring.profiles.active=render` |
| Webhook Service | `mvn clean package -DskipTests -pl webhook-service -am` | `java -jar webhook-service/target/webhook-service-1.0.0-SNAPSHOT.jar --spring.profiles.active=render` |
| Refund Service | `mvn clean package -DskipTests -pl refund-service -am` | `java -jar refund-service/target/refund-service-1.0.0-SNAPSHOT.jar --spring.profiles.active=render` |
| Settlement Service | `mvn clean package -DskipTests -pl settlement-service -am` | `java -jar settlement-service/target/settlement-service-1.0.0-SNAPSHOT.jar --spring.profiles.active=render` |
| Reconciliation Service | `mvn clean package -DskipTests -pl reconciliation-service -am` | `java -jar reconciliation-service/target/reconciliation-service-1.0.0-SNAPSHOT.jar --spring.profiles.active=render` |
| Fraud Monitoring Service | `mvn clean package -DskipTests -pl fraud-monitoring-service -am` | `java -jar fraud-monitoring-service/target/fraud-monitoring-service-1.0.0-SNAPSHOT.jar --spring.profiles.active=render` |
| Notification Service | `mvn clean package -DskipTests -pl notification-service -am` | `java -jar notification-service/target/notification-service-1.0.0-SNAPSHOT.jar --spring.profiles.active=render` |

## Health Check

Use this Render health check path for every web service:

```text
/actuator/health
```

## Frontend

Preferred option: deploy `frontend-react` as a Render Static Site.

```text
Root Directory: frontend-react
Build Command: npm install && npm run build
Publish Directory: dist
Environment Variable:
VITE_API_BASE_URL=https://<api-gateway-service>.onrender.com
```

Alternative option: deploy it as a Docker Web Service.

```text
Root Directory: frontend-react
Docker Build Context Directory: .
Dockerfile Path: ./Dockerfile
Health Check Path: /
Environment Variable:
VITE_API_BASE_URL=https://<api-gateway-service>.onrender.com
```

The frontend Docker image uses nginx and listens on Render's `$PORT` automatically. For either option, set:

```text
VITE_API_BASE_URL=https://<api-gateway-service>.onrender.com
```

Also include the frontend Render URL in `CORS_ALLOWED_ORIGINS` on the API gateway.

## Notes

- Render profile uses `server.port=${PORT:8080}`.
- PostgreSQL dialect is explicitly configured.
- Kafka publishers log and skip event send when `KAFKA_ENABLED=false`.
- Notification Kafka listener is not created when `KAFKA_ENABLED=false`.
- Redis health checks are disabled for Render in Redis-using services.
