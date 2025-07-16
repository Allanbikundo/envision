# Base stage for dependency resolution
FROM maven:3.9.6-eclipse-temurin-17-alpine AS base
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Test stage - runs tests in parallel
FROM base AS test
COPY src ./src
# Run tests with parallel execution and generate reports
RUN mvn test -T 1C -Dmaven.test.failure.ignore=false \
    -Dsurefire.useFile=false \
    -Dsurefire.printSummary=true

# Build stage - compiles and packages (can run in parallel with tests)
FROM base AS build
COPY src ./src
# Build without running tests (already done in test stage)
RUN mvn package -DskipTests -T 1C

# Runtime stage
FROM eclipse-temurin:17-jre-alpine AS production

# Set environment variables
ENV SPRING_OUTPUT_ANSI_ENABLED=ALWAYS \
    JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseG1GC -XX:+HeapDumpOnOutOfMemoryError -Dfile.encoding=UTF-8 -Djava.security.egd=file:/dev/./urandom" \
    TZ=Africa/Nairobi

# Set timezone
RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/Africa/Nairobi /etc/localtime && \
    echo "Africa/Nairobi" > /etc/timezone && \
    apk del tzdata

# Create a non-root user
RUN addgroup -S appuser && adduser -S appuser -G appuser

# Create app directory and set permissions
RUN mkdir -p /app && chown -R appuser:appuser /app
WORKDIR /app

# Copy the JAR file from the build stage
COPY --from=build /app/target/*.jar app.jar

# Set non-root user
USER appuser

# Expose the port the app runs on
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
    CMD wget --quiet --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]