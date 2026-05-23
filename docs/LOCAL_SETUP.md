# Local Setup

## Prerequisites

- Java 21 LTS
- Maven running on Java 21
- PostgreSQL 16 or Docker Desktop
- Node 20 for `frontend-react`

## Local Environment

Use local profile values through environment variables or `application-local.yml`.

```powershell
$env:SPRING_PROFILES_ACTIVE='local'
$env:DATABASE_URL='jdbc:postgresql://localhost:5432/payment_platform_db'
$env:DB_USERNAME='postgres'
$env:DB_PASSWORD='root123'
$env:KAFKA_ENABLED='true'
$env:REDIS_ENABLED='true'
```

For Docker Compose, create a local `.env` file if you want to override defaults. `.env` is ignored by Git.

```text
POSTGRES_HOST_PORT=5433
LOCAL_POSTGRES_DB=payment_platform_db
LOCAL_DB_USERNAME=payment
LOCAL_DB_PASSWORD=<LOCAL_ONLY_PASSWORD>
JWT_SECRET=<LOCAL_LONG_SECRET>
```

## Build

```powershell
mvn clean install -DskipTests
```

## Start Backend Manually

Start services in this order:

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

## Docker Compose

Build jars before Compose copies them into images.

```powershell
mvn clean package -DskipTests
$env:POSTGRES_HOST_PORT='5433'
docker compose up --build -d
docker compose ps
```

Stop without deleting volumes:

```powershell
docker compose stop
```

Remove containers and network:

```powershell
docker compose down
```

## Frontend

```powershell
cd frontend-react
npm install
$env:VITE_API_BASE_URL='http://localhost:8080'
npm run dev
```

## Health Checks

```powershell
Invoke-WebRequest http://localhost:8761/actuator/health -UseBasicParsing
Invoke-WebRequest http://localhost:8080/actuator/health -UseBasicParsing
Invoke-WebRequest http://localhost:8081/actuator/health -UseBasicParsing
```

