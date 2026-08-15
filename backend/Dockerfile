# ==========================================
# Stage 1: Build the Application
# ==========================================
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app

# Step 1: Copy the pom.xml and download dependencies first.
# This caches the dependencies layer, significantly speeding up future builds.
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Step 2: Copy the actual source code and build the executable JAR.
COPY src ./src
RUN mvn clean package -DskipTests

# ==========================================
# Stage 2: Create the Lightweight Runtime Image
# ==========================================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Step 3: Copy ONLY the compiled JAR from the builder stage into the runtime image.
COPY --from=builder /app/target/*.jar app.jar

# Step 4: Expose the port the Spring Boot app runs on
EXPOSE 8080

# Step 5: Execute the application
ENTRYPOINT ["java", "-jar", "app.jar"]