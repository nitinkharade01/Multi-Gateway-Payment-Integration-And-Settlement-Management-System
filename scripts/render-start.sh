#!/usr/bin/env sh
set -eu

APP_DIR="${APP_DIR:-/opt/payment-platform}"
PROFILE="${SPRING_PROFILES_ACTIVE:-render}"
PUBLIC_PORT="${PORT:-8080}"
ENABLED_SERVICES="${ENABLED_SERVICES:-all}"
API_GATEWAY_DELAY_SECONDS="${API_GATEWAY_STARTUP_DELAY_SECONDS:-2}"
DISCOVERY_DELAY_SECONDS="${DISCOVERY_STARTUP_DELAY_SECONDS:-20}"
SERVICE_DELAY_SECONDS="${SERVICE_STARTUP_DELAY_SECONDS:-2}"
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
KNOWN_SERVICES="auth-service merchant-service gateway-routing-service payment-service webhook-service refund-service settlement-service reconciliation-service fraud-monitoring-service notification-service"

log() {
  printf '%s %s\n' "$(date -u '+%Y-%m-%dT%H:%M:%SZ')" "$*"
}

jar_for() {
  printf '%s-1.0.0-SNAPSHOT.jar' "$1"
}

require_jar() {
  required_jar_path="$APP_DIR/$1"
  if [ ! -f "$required_jar_path" ]; then
    log "ERROR missing required jar: $required_jar_path"
    exit 1
  fi
}

validate_enabled_services() {
  if [ "$ENABLED_SERVICES" = "all" ]; then
    return
  fi

  for service in $(printf '%s' "$ENABLED_SERVICES" | tr ',' ' '); do
    case " $KNOWN_SERVICES " in
      *" $service "*) ;;
      *)
        log "ERROR unknown service in ENABLED_SERVICES: $service"
        log "Allowed values: all or comma-separated subset of: $KNOWN_SERVICES"
        exit 1
        ;;
    esac
  done
}

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
  port="$2"
  jar="$(jar_for "$name")"
  require_jar "$jar"

  log "Starting $name on port $port with profile $PROFILE"
  # shellcheck disable=SC2086
  java $JAVA_OPTS -jar "$APP_DIR/$jar" \
    --spring.profiles.active="$PROFILE" \
    --server.port="$port" &
  pid="$!"
  PIDS="$PIDS $pid"
  log "$name started with pid $pid"
}

stop_all() {
  log "Stopping backend services"
  for pid in $PIDS; do
    kill "$pid" 2>/dev/null || true
  done
  wait 2>/dev/null || true
}

trap stop_all INT TERM

validate_enabled_services
require_jar "$(jar_for discovery-server)"
require_jar "$(jar_for api-gateway)"

log "Render all-in-one startup"
log "Public API Gateway port: $PUBLIC_PORT"
log "Enabled internal services: $ENABLED_SERVICES"
log "Eureka URL: $EUREKA_SERVER_URL"
log "Kafka enabled: $KAFKA_ENABLED"
log "Redis enabled: $REDIS_ENABLED"

start_service "discovery-server" "8761"
log "Waiting ${API_GATEWAY_DELAY_SECONDS}s before starting public api-gateway"
sleep "$API_GATEWAY_DELAY_SECONDS"

start_service "api-gateway" "$PUBLIC_PORT"
log "Waiting ${DISCOVERY_DELAY_SECONDS}s for discovery/api-gateway startup before internal services"
sleep "$DISCOVERY_DELAY_SECONDS"

for service in $KNOWN_SERVICES; do
  if is_enabled "$service"; then
    case "$service" in
      auth-service) port="8081" ;;
      merchant-service) port="8082" ;;
      payment-service) port="8083" ;;
      gateway-routing-service) port="8084" ;;
      webhook-service) port="8085" ;;
      refund-service) port="8086" ;;
      settlement-service) port="8087" ;;
      reconciliation-service) port="8088" ;;
      fraud-monitoring-service) port="8089" ;;
      notification-service) port="8090" ;;
    esac
    start_service "$service" "$port"
    sleep "$SERVICE_DELAY_SECONDS"
  else
    log "Skipping disabled service: $service"
  fi
done

log "Startup complete. Monitoring child JVMs."
while true; do
  for pid in $PIDS; do
    if ! kill -0 "$pid" 2>/dev/null; then
      log "ERROR backend process $pid exited"
      wait "$pid"
      exit "$?"
    fi
  done
  sleep 5
done
