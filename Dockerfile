# Use multi-stage build for optimization
FROM maven:3.8.4-openjdk-17-slim AS build

# Set working directory
WORKDIR /app

# Copy pom.xml separately to leverage Docker cache
COPY pom.xml .

# Download dependencies
RUN mvn dependency:go-offline

# Copy source code
COPY src ./src

# Build the application
RUN mvn package -DskipTests

# Create a minimal runtime image
FROM openjdk:17-jdk-slim

# Set working directory
WORKDIR /app

# Copy built jar from the build stage
COPY --from=build /app/target/*.jar app.jar

# Copy test resources
COPY --from=build /app/src/test/resources ./src/test/resources

# Install curl for health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Expose port if needed (e.g., for Allure report server)
EXPOSE 8080

# Set entrypoint to run tests and generate Allure report
ENTRYPOINT ["java", "-jar", "app.jar"]

# Default command to run tests and generate report
CMD ["mvn", "test", "allure:report"]