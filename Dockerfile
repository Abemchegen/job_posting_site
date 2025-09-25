# Use a Java 21 base image
FROM eclipse-temurin:21-jdk-alpine

# Set working directory
WORKDIR /app

# Copy Maven project files and build
COPY pom.xml .
COPY src ./src

# Build the app
RUN ./mvnw clean package -DskipTests

# Copy the jar
COPY target/*.jar app.jar

# Expose port
EXPOSE 8080

# Run the app
ENTRYPOINT ["java", "-jar", "app.jar"]
