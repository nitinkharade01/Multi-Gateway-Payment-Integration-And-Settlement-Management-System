# Architecture

## Overview

The platform is a Java 21 Maven multi-module microservices system for a payment gateway integration and settlement domain.

```text
React frontend
  -> API Gateway
     -> Auth Service
     -> Merchant Service
     -> Payment Service -> Merchant Service
                       -> Gateway Routing Service
     -> Webhook Service -> Payment Service
     -> Refund Service -> Payment Service
     -> Settlement Service -> Payment Service
     -> Reconciliation Service -> Payment Service
     -> Fraud Monitoring Service
     -> Notification Service

Eureka Discovery Server registers backend services.
PostgreSQL stores service-owned domain tables.
Kafka is optional for domain events.
Redis is optional for local caching support.
```

## Services

| Service | Responsibility |
| --- | --- |
| `discovery-server` | Eureka service registry |
| `api-gateway` | Public routing, CORS, correlation ID propagation |
| `auth-service` | Registration, login, JWT, refresh token persistence |
| `merchant-service` | Merchant onboarding, KYC status, webhook URL, API keys |
| `payment-service` | Idempotent order creation, transactions, status updates |
| `gateway-routing-service` | Payment mode to gateway routing and gateway health simulation |
| `webhook-service` | Gateway webhook signature validation, replay protection, deduplication |
| `refund-service` | Full and partial refund workflows |
| `settlement-service` | Settlement batch generation and fee/GST/net calculations |
| `reconciliation-service` | Gateway CSV parsing and mismatch detection |
| `fraud-monitoring-service` | Risk scoring and alert review |
| `notification-service` | Email and merchant webhook callback simulation |

## Event Mode

Kafka is enabled locally by default and disabled in Render profile by default. With Kafka disabled, event publishers log the event and continue the business flow in demo mode.

## Data Mode

Each DB-backed service uses PostgreSQL through `DATABASE_URL`, `DB_USERNAME`, and `DB_PASSWORD` in Render profile. Hibernate uses `ddl-auto=update` for deployment convenience.

