FROM eclipse-temurin:21-jdk

ARG SERVICE_JAR
WORKDIR /opt/payment-platform
COPY ${SERVICE_JAR} app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-jar", "/opt/payment-platform/app.jar"]
