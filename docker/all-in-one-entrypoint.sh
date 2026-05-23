#!/bin/sh
set -eu

PROFILE="${SPRING_PROFILES_ACTIVE:-render}"
PUBLIC_PORT="${PORT:-8080}"
API_GATEWAY_DELAY_SECONDS="${API_GATEWAY_STARTUP_DELAY_SECONDS:-2}"
DISCOVERY_DELAY_SECONDS="${DISCOVERY_STARTUP_DELAY_SECONDS:-20}"
SERVICE_STARTUP_DELAY_SECONDS="${SERVICE_STARTUP_DELAY_SECONDS:-2}"
ENABLED_SERVICES="${ENABLED_SERVICES:-all}"
JAVA_OPTS="${JAVA_OPTS:--XX:+UseContainerSupport -XX:+UseSerialGC -XX:MaxRAMPercentage=8.0}"

export SPRING_PROFILES_ACTIVE="$PROFILE"
export EUREKA_SERVER_URL="${EUREKA_SERVER_URL:-http://localhost:8761/eureka/}"
export KAFKA_ENABLED="${KAFKA_ENABLED:-false}"
export REDIS_ENABLED="${REDIS_ENABLED:-false}"
export MERCHANT_SERVICE_URL="${MERCHANT_SERVICE_URL:-http://localhost:8082}"
export ROUTING_SERVICE_URL="${ROUTING_SERVICE_URL:-http://localhost:8084}"
export PAYMENT_SERVICE_URL="${PAYMENT_SERVICE_URL:-http://localhost:8083}"
export CORS_ALLOWED_ORIGINS="${CORS_ALLOWED_ORIGINS:-http://localhost:3000,http://localhost:5173}"

PIDS=""

is_enabled() {
  if [ "$ENABLED_SERVICES" = "all" ]; then
    return 0
  fi

  case ",$ENABLED_SERVICES," in
    *",$1,"*) return 0 ;;
    *) return 1 ;;
  esac
}

start_service() {
  name="$1"
  jar="$2"
  port="$3"

  echo "Starting $name on port $port"
  # shellcheck disable=SC2086
  java $JAVA_OPTS -jar "/opt/payment-platform/$jar" \
    --spring.profiles.active="$PROFILE" \
    --server.port="$port" &
  pid="$!"
  PIDS="$PIDS $pid"
}

stop_all() {
  echo "Stopping backend services"
  for pid in $PIDS; do
    kill "$pid" 2>/dev/null || true
  done
  wait 2>/dev/null || true
}

trap stop_all INT TERM

start_service "discovery-server" "discovery-server-1.0.0-SNAPSHOT.jar" "8761"
echo "Waiting ${API_GATEWAY_DELAY_SECONDS}s before starting public api-gateway on Render port ${PUBLIC_PORT}"
sleep "$API_GATEWAY_DELAY_SECONDS"

start_service "api-gateway" "api-gateway-1.0.0-SNAPSHOT.jar" "$PUBLIC_PORT"

echo "Waiting ${DISCOVERY_DELAY_SECONDS}s before starting internal backend services"
sleep "$DISCOVERY_DELAY_SECONDS"

if is_enabled "auth-service"; then
  start_service "auth-service" "auth-service-1.0.0-SNAPSHOT.jar" "8081"
  sleep "$SERVICE_STARTUP_DELAY_SECONDS"
fi
if is_enabled "merchant-service"; then
  start_service "merchant-service" "merchant-service-1.0.0-SNAPSHOT.jar" "8082"
  sleep "$SERVICE_STARTUP_DELAY_SECONDS"
fi
if is_enabled "gateway-routing-service"; then
  start_service "gateway-routing-service" "gateway-routing-service-1.0.0-SNAPSHOT.jar" "8084"
  sleep "$SERVICE_STARTUP_DELAY_SECONDS"
fi
if is_enabled "payment-service"; then
  start_service "payment-service" "payment-service-1.0.0-SNAPSHOT.jar" "8083"
  sleep "$SERVICE_STARTUP_DELAY_SECONDS"
fi
if is_enabled "webhook-service"; then
  start_service "webhook-service" "webhook-service-1.0.0-SNAPSHOT.jar" "8085"
  sleep "$SERVICE_STARTUP_DELAY_SECONDS"
fi
if is_enabled "refund-service"; then
  start_service "refund-service" "refund-service-1.0.0-SNAPSHOT.jar" "8086"
  sleep "$SERVICE_STARTUP_DELAY_SECONDS"
fi
if is_enabled "settlement-service"; then
  start_service "settlement-service" "settlement-service-1.0.0-SNAPSHOT.jar" "8087"
  sleep "$SERVICE_STARTUP_DELAY_SECONDS"
fi
if is_enabled "reconciliation-service"; then
  start_service "reconciliation-service" "reconciliation-service-1.0.0-SNAPSHOT.jar" "8088"
  sleep "$SERVICE_STARTUP_DELAY_SECONDS"
fi
if is_enabled "fraud-monitoring-service"; then
  start_service "fraud-monitoring-service" "fraud-monitoring-service-1.0.0-SNAPSHOT.jar" "8089"
  sleep "$SERVICE_STARTUP_DELAY_SECONDS"
fi
if is_enabled "notification-service"; then
  start_service "notification-service" "notification-service-1.0.0-SNAPSHOT.jar" "8090"
fi

while true; do
  for pid in $PIDS; do
    if ! kill -0 "$pid" 2>/dev/null; then
      echo "Backend process $pid exited"
      wait "$pid"
      exit "$?"
    fi
  done
  sleep 5
done
