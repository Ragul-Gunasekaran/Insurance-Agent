# ==============================================================================
# Multi-stage Dockerfile for TrustDrive Spring Boot Backend (Java 21 LTS)
# ==============================================================================

# Stage 1: Build executable JAR using Eclipse Temurin Java 21 JDK
FROM eclipse-temurin:21-jdk-jammy AS builder
WORKDIR /app

# Cache Maven wrapper and dependencies
COPY backend/.mvn/ .mvn/
COPY backend/mvnw backend/pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B || true

# Copy source code and package executable production archive
COPY backend/src/ src/
RUN ./mvnw clean package -DskipTests

# Stage 2: Minimal, secure runtime image using Eclipse Temurin Java 21 JRE
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Non-root user for principle of least privilege
RUN groupadd -r ubi && useradd -r -g ubi ubi

# Copy packaged Spring Boot JAR from builder stage
COPY --from=builder /app/target/usage-based-insurance-backend-1.0.0-SNAPSHOT.jar app.jar
RUN chown -R ubi:ubi /app

USER ubi

# Default HTTP port (Render injects PORT dynamically)
EXPOSE 8080

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dserver.port=${PORT:-8080} -jar app.jar"]
