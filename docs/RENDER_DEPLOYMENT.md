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

Deploy `frontend-react` as a static site or Docker web service. Set:

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

