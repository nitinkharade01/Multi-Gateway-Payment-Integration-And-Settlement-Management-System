FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /workspace
COPY . .
RUN mvn -B clean package -DskipTests
RUN mkdir -p /workspace/app-jars \
    && cp discovery-server/target/discovery-server-1.0.0-SNAPSHOT.jar /workspace/app-jars/ \
    && cp api-gateway/target/api-gateway-1.0.0-SNAPSHOT.jar /workspace/app-jars/ \
    && cp auth-service/target/auth-service-1.0.0-SNAPSHOT.jar /workspace/app-jars/ \
    && cp merchant-service/target/merchant-service-1.0.0-SNAPSHOT.jar /workspace/app-jars/ \
    && cp payment-service/target/payment-service-1.0.0-SNAPSHOT.jar /workspace/app-jars/ \
    && cp gateway-routing-service/target/gateway-routing-service-1.0.0-SNAPSHOT.jar /workspace/app-jars/ \
    && cp webhook-service/target/webhook-service-1.0.0-SNAPSHOT.jar /workspace/app-jars/ \
    && cp refund-service/target/refund-service-1.0.0-SNAPSHOT.jar /workspace/app-jars/ \
    && cp settlement-service/target/settlement-service-1.0.0-SNAPSHOT.jar /workspace/app-jars/ \
    && cp reconciliation-service/target/reconciliation-service-1.0.0-SNAPSHOT.jar /workspace/app-jars/ \
    && cp fraud-monitoring-service/target/fraud-monitoring-service-1.0.0-SNAPSHOT.jar /workspace/app-jars/ \
    && cp notification-service/target/notification-service-1.0.0-SNAPSHOT.jar /workspace/app-jars/

FROM eclipse-temurin:21-jre

WORKDIR /opt/payment-platform
COPY --from=build /workspace/app-jars/*.jar ./
COPY scripts/render-start.sh ./render-start.sh
RUN chmod +x ./render-start.sh
EXPOSE 8080
ENTRYPOINT ["/opt/payment-platform/render-start.sh"]
