# Deployment Checklist

- Confirm Java 21 with `java -version` and `mvn -version`.
- Run `mvn clean install -DskipTests`.
- Confirm `.env` is not committed.
- Set Render `SPRING_PROFILES_ACTIVE=render`.
- Set Render PostgreSQL JDBC URL in `DATABASE_URL`.
- Set `DB_PASSWORD` only in Render environment.
- Set `JWT_SECRET` and webhook secrets in Render environment.
- Deploy discovery first, then update `EUREKA_SERVER_URL` everywhere.
- Keep `KAFKA_ENABLED=false` and `REDIS_ENABLED=false` for first free-tier deployment.
- Use `/actuator/health` as the health check path.

