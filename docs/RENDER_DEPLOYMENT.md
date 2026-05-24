# Render Deployment Guide

This project can be deployed to Render either as separate Java web services or as one Docker web service. Keep all passwords, JWT secrets, webhook secrets, database passwords, API keys, and peppers in Render environment variables only.

## Project Structure

Backend Maven modules:

- `platform-common`
- `discovery-server`
- `api-gateway`
- `auth-service`
- `merchant-service`
- `payment-service`
- `gateway-routing-service`
- `webhook-service`
- `refund-service`
- `settlement-service`
- `reconciliation-service`
- `fraud-monitoring-service`
- `notification-service`

The root `pom.xml` is the Maven multi-module parent. Every Spring Boot web service has an `application-render.yml` profile and exposes:

```text
/actuator/health
```

Use this Render health check path for every backend web service:

```text
/actuator/health
```

## PostgreSQL

Create a Render PostgreSQL database first. Render may show an internal URL like:

```text
postgresql://payment_user:<DB_PASSWORD>@dpg-d88jb6h9rddc738g4nv0-a/payment_platform_db
```

Spring Boot must receive the JDBC form:

```text
DATABASE_URL=jdbc:postgresql://dpg-d88jb6h9rddc738g4nv0-a:5432/payment_platform_db
DB_USERNAME=payment_user
DB_PASSWORD=<from Render database>
```

Do not use the plain `postgresql://...` URL as `spring.datasource.url`.

## Common Environment

Set these on every backend service:

```text
SPRING_PROFILES_ACTIVE=render
EUREKA_SERVER_URL=https://<discovery-server-service-name>.onrender.com/eureka/
KAFKA_ENABLED=false
REDIS_ENABLED=false
CORS_ALLOWED_ORIGINS=https://<frontend-service>.onrender.com,http://localhost:3000,http://localhost:5173
```

For DB-backed services also set:

```text
DATABASE_URL=jdbc:postgresql://dpg-d88jb6h9rddc738g4nv0-a:5432/payment_platform_db
DB_USERNAME=payment_user
DB_PASSWORD=<from Render database>
```

DB-backed services:

- `auth-service`
- `merchant-service`
- `payment-service`
- `gateway-routing-service`
- `webhook-service`
- `refund-service`
- `settlement-service`
- `reconciliation-service`
- `fraud-monitoring-service`
- `notification-service`

Non-DB services:

- `discovery-server`
- `api-gateway`

## Secret Environment

Set these only on services that need them. It is also safe to put them in a Render environment group and attach the group to all backend services.

```text
JWT_SECRET=<long random 512-bit signing secret>
JWT_EXPIRATION=86400000
JWT_REFRESH_TTL_DAYS=7
ADMIN_DEFAULT_EMAIL=admin@payment.com
ADMIN_DEFAULT_PASSWORD=<secure initial admin password>
WEBHOOK_SECRET=<secure webhook fallback secret>
API_SECRET_PEPPER=<long random merchant API secret pepper>
```

Optional gateway-specific webhook secrets:

```text
RAZORPAY_WEBHOOK_SECRET=<secure Razorpay webhook secret>
CASHFREE_WEBHOOK_SECRET=<secure Cashfree webhook secret>
PAYU_WEBHOOK_SECRET=<secure PayU webhook secret>
```

If gateway-specific webhook secrets are not set, `webhook-service` falls back to `WEBHOOK_SECRET`.

## Service URLs

For separate Render services:

```text
MERCHANT_SERVICE_URL=https://<merchant-service>.onrender.com
ROUTING_SERVICE_URL=https://<gateway-routing-service>.onrender.com
PAYMENT_SERVICE_URL=https://<payment-service>.onrender.com
```

Use them on these services:

- `payment-service`: `MERCHANT_SERVICE_URL`, `ROUTING_SERVICE_URL`
- `webhook-service`: `PAYMENT_SERVICE_URL`
- `refund-service`: `PAYMENT_SERVICE_URL`
- `settlement-service`: `PAYMENT_SERVICE_URL`
- `reconciliation-service`: `PAYMENT_SERVICE_URL`

For the all-in-one Docker deployment:

```text
EUREKA_SERVER_URL=http://localhost:8761/eureka/
MERCHANT_SERVICE_URL=http://localhost:8082
ROUTING_SERVICE_URL=http://localhost:8084
PAYMENT_SERVICE_URL=http://localhost:8083
```

## Deployment Order

1. Create Render PostgreSQL.
2. Deploy `discovery-server`.
3. Copy the discovery service URL.
4. Set `EUREKA_SERVER_URL=https://<discovery-server-service-name>.onrender.com/eureka/` for all other services.
5. Deploy `api-gateway`.
6. Deploy `auth-service`.
7. Deploy `merchant-service`.
8. Deploy `gateway-routing-service`.
9. Deploy `payment-service`.
10. Deploy optional workflow services: `webhook-service`, `refund-service`, `settlement-service`, `reconciliation-service`, `fraud-monitoring-service`, `notification-service`.
11. Deploy the frontend static site and add its URL to `CORS_ALLOWED_ORIGINS`.

## Separate Java Web Services

Use these Render settings for each backend service:

```text
Type: Web Service
Runtime: Java
Root Directory: leave blank
Health Check Path: /actuator/health
```

Build and start commands:

| Service | Build Command | Start Command |
| --- | --- | --- |
| `discovery-server` | `mvn clean package -DskipTests -pl discovery-server -am` | `java -jar discovery-server/target/discovery-server-1.0.0-SNAPSHOT.jar --spring.profiles.active=render` |
| `api-gateway` | `mvn clean package -DskipTests -pl api-gateway -am` | `java -jar api-gateway/target/api-gateway-1.0.0-SNAPSHOT.jar --spring.profiles.active=render` |
| `auth-service` | `mvn clean package -DskipTests -pl auth-service -am` | `java -jar auth-service/target/auth-service-1.0.0-SNAPSHOT.jar --spring.profiles.active=render` |
| `merchant-service` | `mvn clean package -DskipTests -pl merchant-service -am` | `java -jar merchant-service/target/merchant-service-1.0.0-SNAPSHOT.jar --spring.profiles.active=render` |
| `payment-service` | `mvn clean package -DskipTests -pl payment-service -am` | `java -jar payment-service/target/payment-service-1.0.0-SNAPSHOT.jar --spring.profiles.active=render` |
| `gateway-routing-service` | `mvn clean package -DskipTests -pl gateway-routing-service -am` | `java -jar gateway-routing-service/target/gateway-routing-service-1.0.0-SNAPSHOT.jar --spring.profiles.active=render` |
| `webhook-service` | `mvn clean package -DskipTests -pl webhook-service -am` | `java -jar webhook-service/target/webhook-service-1.0.0-SNAPSHOT.jar --spring.profiles.active=render` |
| `refund-service` | `mvn clean package -DskipTests -pl refund-service -am` | `java -jar refund-service/target/refund-service-1.0.0-SNAPSHOT.jar --spring.profiles.active=render` |
| `settlement-service` | `mvn clean package -DskipTests -pl settlement-service -am` | `java -jar settlement-service/target/settlement-service-1.0.0-SNAPSHOT.jar --spring.profiles.active=render` |
| `reconciliation-service` | `mvn clean package -DskipTests -pl reconciliation-service -am` | `java -jar reconciliation-service/target/reconciliation-service-1.0.0-SNAPSHOT.jar --spring.profiles.active=render` |
| `fraud-monitoring-service` | `mvn clean package -DskipTests -pl fraud-monitoring-service -am` | `java -jar fraud-monitoring-service/target/fraud-monitoring-service-1.0.0-SNAPSHOT.jar --spring.profiles.active=render` |
| `notification-service` | `mvn clean package -DskipTests -pl notification-service -am` | `java -jar notification-service/target/notification-service-1.0.0-SNAPSHOT.jar --spring.profiles.active=render` |

## All-In-One Docker Web Service

The root `Dockerfile` builds all backend JAR files and starts services with `scripts/render-start.sh`. API Gateway is the only public entrypoint and binds to Render's `$PORT`. Internal services run on fixed local ports.

Render settings:

```text
Type: Web Service
Runtime: Docker
Root Directory: leave blank
Docker Build Context Directory: .
Dockerfile Path: ./Dockerfile
Docker Command: leave blank
Health Check Path: /actuator/health
```

Required all-in-one environment:

```text
SPRING_PROFILES_ACTIVE=render
EUREKA_SERVER_URL=http://localhost:8761/eureka/
KAFKA_ENABLED=false
REDIS_ENABLED=false
MERCHANT_SERVICE_URL=http://localhost:8082
ROUTING_SERVICE_URL=http://localhost:8084
PAYMENT_SERVICE_URL=http://localhost:8083
ENABLED_SERVICES=all
API_GATEWAY_STARTUP_DELAY_SECONDS=2
DISCOVERY_STARTUP_DELAY_SECONDS=20
SERVICE_STARTUP_DELAY_SECONDS=2
```

For a smaller all-in-one demo:

```text
ENABLED_SERVICES=auth-service,merchant-service,gateway-routing-service,payment-service
```

The script always starts `discovery-server` and `api-gateway`; `ENABLED_SERVICES` controls the internal backend services.

## Free-Tier Recommendation

For Render Free, deploy only:

- `discovery-server`
- `api-gateway`
- `auth-service`
- `merchant-service`
- `gateway-routing-service`
- `payment-service`

Keep:

```text
KAFKA_ENABLED=false
REDIS_ENABLED=false
```

The full all-in-one backend may run out of memory on Render Free. Use a smaller `ENABLED_SERVICES` list or upgrade to at least 2 GB RAM for the full platform.

## Frontend Static Site

Deploy `frontend-react` as a Render Static Site:

```text
Root Directory: frontend-react
Build Command: npm install && npm run build
Publish Directory: dist
```

Environment variable:

```text
VITE_API_BASE_URL=https://<api-gateway-service>.onrender.com
```

The React app reads `import.meta.env.VITE_API_BASE_URL` in `frontend-react/src/api.js`.

## Troubleshooting

- Health check fails: confirm `/actuator/health`, `SPRING_PROFILES_ACTIVE=render`, and that the service binds to Render's `PORT`.
- Database connection fails: use the JDBC URL format `jdbc:postgresql://host:5432/database`, not `postgresql://...`.
- Hibernate dialect errors: confirm `DATABASE_URL`, `DB_USERNAME`, and `DB_PASSWORD` are set on DB-backed services.
- Kafka startup or send issues: keep `KAFKA_ENABLED=false` on Render Free. Publishers log and skip event publishing when disabled.
- Redis health failures: keep `REDIS_ENABLED=false`; the render profile disables Redis health in Redis-using services.
- Gateway returns service unavailable: confirm the target service is deployed and registered in Eureka, and `EUREKA_SERVER_URL` ends with `/eureka/`.
- CORS errors: include the frontend URL in `CORS_ALLOWED_ORIGINS` on `api-gateway`.
- Docker all-in-one exits early: check Render logs for missing JAR messages from `scripts/render-start.sh`.
