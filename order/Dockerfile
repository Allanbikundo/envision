# Build stage
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build
WORKDIR /app

# Copy only the POM file first to leverage Docker cache
COPY pom.xml .

# Download all dependencies (this step is cached as long as pom.xml doesn't change)
RUN mvn dependency:go-offline -B

# Copy the rest of the source code
COPY src ./src

# Build the application
RUN mvn package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre-alpine

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

# Expose the port the app runs on (default Spring Boot port)
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
    CMD wget --quiet --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]