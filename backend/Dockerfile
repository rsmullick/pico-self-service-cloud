# Multi-stage Dockerfile for backend

FROM maven:3.9.4-eclipse-temurin-21 AS builder
WORKDIR /workspace

# copy pom files and download dependencies
COPY pom.xml ./
COPY backend/pom.xml ./backend/pom.xml
RUN mvn -B -f backend/pom.xml dependency:go-offline

# copy source
COPY backend/src ./backend/src

# build
RUN mvn -B -f backend/pom.xml clean package -DskipTests

# runtime image
FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=builder /workspace/backend/target/*.jar app.jar

EXPOSE 8080

# Enable virtual threads scheduler parallelism by default; can be overridden
ENV JAVA_OPTS="-Djdk.virtualThreadScheduler.parallelism=8"

HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
